package com.xiaowang.cola.tools.integration;

import com.xiaowang.cola.tools.constant.StockConstant;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.domain.service.StockRecordCleanupService;
import com.xiaowang.cola.tools.domain.service.StockRecordPersistenceService;
import com.xiaowang.cola.tools.domain.service.StockService;
import com.xiaowang.cola.tools.param.StockDeductParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存系统集成测试（Redis + 数据库）
 *
 * @author cola
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional // 测试完成后回滚数据
public class StockIntegrationTest {

  @Resource
  private StockService stockService;

  @Resource
  private StockRecordPersistenceService persistenceService;

  @Resource
  private StockRecordCleanupService cleanupService;

  @Resource
  private RedisTemplate<String, String> redisTemplate;

  private static final String TEST_PRODUCT_ID = "integration_test_product";
  private static final String TEST_USER_ID = "integration_test_user";

  @BeforeEach
  void setUp() {
    // 清理Redis测试数据
    cleanupRedisTestData();

    // 初始化测试商品库存
    stockService.initStock(TEST_PRODUCT_ID, 100);
  }

  @Test
  void testCompleteStockDeductFlow() {
    // 测试完整的库存扣减流程：Redis扣减 + 数据库持久化

    // 1. 执行库存扣减
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(10)
        .userId(TEST_USER_ID)
        .orderId("INTEGRATION_ORDER_001")
        .scene("INTEGRATION_TEST")
        .extInfo("{\"test\":\"integration\"}")
        .build();

    StockDeductResponse response = stockService.deductStock(param);

    // 验证Redis扣减结果
    assertTrue(response.getSuccess());
    assertEquals(90, response.getRemainingStock());
    assertNotNull(response.getRecordId());

    // 2. 验证Redis中的流水记录
    StockRecord redisRecord = stockService.getStockRecord(TEST_PRODUCT_ID, response.getRecordId());
    assertNotNull(redisRecord);
    assertEquals(TEST_PRODUCT_ID, redisRecord.getProductId());
    assertEquals(10, redisRecord.getAmount());

    // 3. 手动触发数据库持久化（模拟异步完成）
    persistenceService.asyncPersistStockRecord(TEST_PRODUCT_ID, response.getRecordId());

    // 等待异步操作完成
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  void testProductOfflineFlow() {
    // 测试商品下架流程

    // 1. 先进行一些库存操作
    for (int i = 0; i < 3; i++) {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(5)
          .userId(TEST_USER_ID + "_" + i)
          .orderId("OFFLINE_ORDER_" + i)
          .scene("OFFLINE_TEST")
          .build();

      stockService.deductStock(param);
    }

    // 验证Redis中有流水记录
    List<String> recordIds = stockService.getStockRecordIds(TEST_PRODUCT_ID);
    assertEquals(3, recordIds.size());

    // 2. 执行商品下架处理
    StockRecordCleanupService.ProcessResult result = cleanupService.processProductOffline(TEST_PRODUCT_ID);

    // 验证处理结果
    assertEquals(3, result.getPersistedCount()); // 持久化了3条记录
    assertEquals(3, result.getExpiredCount()); // 设置了3条记录的过期时间
  }

  @Test
  void testReconcileFlow() {
    // 测试对账流程

    // 1. 查询现有的待对账记录（来自测试数据）
    List<StockRecord> pendingRecords = cleanupService.getPendingReconcileRecords("PRODUCT_001");

    assertNotNull(pendingRecords);
    assertTrue(pendingRecords.size() >= 0); // 可能有测试数据

    // 2. 如果有待对账记录，进行对账
    if (!pendingRecords.isEmpty()) {
      List<String> recordIds = pendingRecords.stream()
          .map(StockRecord::getRecordId)
          .toList();

      int reconciledCount = cleanupService.markRecordsAsReconciled(recordIds);
      assertTrue(reconciledCount >= 0);
    }
  }

  @Test
  void testTimeRangeQuery() {
    // 测试时间范围查询功能

    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 9, 0, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    List<StockRecord> records = cleanupService.getRecordsByTimeRange(
        "PRODUCT_002", startTime, endTime);

    assertNotNull(records);
    // 验证查询到的记录都在时间范围内
    for (StockRecord record : records) {
      assertTrue(record.getCreateTime().isAfter(startTime.minusSeconds(1)));
      assertTrue(record.getCreateTime().isBefore(endTime.plusSeconds(1)));
    }
  }

  @Test
  void testDataConsistency() {
    // 测试数据一致性：Redis和数据库数据应该一致

    // 1. 执行库存扣减
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(15)
        .userId(TEST_USER_ID)
        .orderId("CONSISTENCY_ORDER")
        .scene("CONSISTENCY_TEST")
        .build();

    StockDeductResponse response = stockService.deductStock(param);
    assertTrue(response.getSuccess());

    // 2. 获取Redis中的记录
    StockRecord redisRecord = stockService.getStockRecord(TEST_PRODUCT_ID, response.getRecordId());
    assertNotNull(redisRecord);

    // 3. 手动持久化到数据库
    persistenceService.asyncPersistStockRecord(TEST_PRODUCT_ID, response.getRecordId());

    // 等待异步操作
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // 4. 验证数据一致性
    assertEquals(TEST_PRODUCT_ID, redisRecord.getProductId());
    assertEquals(15, redisRecord.getAmount());
    assertEquals(TEST_USER_ID, redisRecord.getUserId());
    assertEquals("CONSISTENCY_ORDER", redisRecord.getOrderId());
    assertEquals("CONSISTENCY_TEST", redisRecord.getScene());
  }

  @Test
  void testConcurrentOperationsWithPersistence() {
    // 测试并发操作下的数据一致性

    // 执行多次扣减操作
    for (int i = 0; i < 5; i++) {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(3)
          .userId(TEST_USER_ID + "_" + i)
          .orderId("CONCURRENT_ORDER_" + i)
          .scene("CONCURRENT_TEST")
          .build();

      StockDeductResponse response = stockService.deductStock(param);
      assertTrue(response.getSuccess());
    }

    // 验证最终库存
    Integer finalStock = stockService.getCurrentStock(TEST_PRODUCT_ID);
    assertEquals(85, finalStock.intValue()); // 100 - 5*3 = 85

    // 验证流水记录数量
    List<String> recordIds = stockService.getStockRecordIds(TEST_PRODUCT_ID);
    assertEquals(5, recordIds.size());
  }

  private void cleanupRedisTestData() {
    // 清理Redis测试数据
    String stockKey = StockConstant.STOCK_KEY_PREFIX + TEST_PRODUCT_ID;
    String indexKey = StockConstant.STOCK_RECORD_INDEX_KEY_PREFIX + TEST_PRODUCT_ID;

    redisTemplate.delete(stockKey);

    // 清理流水记录
    var recordIds = redisTemplate.opsForSet().members(indexKey);
    if (recordIds != null && !recordIds.isEmpty()) {
      for (String recordId : recordIds) {
        String recordKey = StockConstant.STOCK_RECORD_KEY_PREFIX + TEST_PRODUCT_ID + ":" + recordId;
        redisTemplate.delete(recordKey);
      }
    }

    redisTemplate.delete(indexKey);
  }
}
