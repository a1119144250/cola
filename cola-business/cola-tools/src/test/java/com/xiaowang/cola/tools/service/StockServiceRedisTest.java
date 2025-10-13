package com.xiaowang.cola.tools.service;

import com.xiaowang.cola.tools.constant.StockConstant;
import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.domain.service.StockService;
import com.xiaowang.cola.tools.param.StockDeductParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存服务Redis功能测试（不依赖数据库）
 *
 * @author cola
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-redis-test.yml")
public class StockServiceRedisTest {

  @Resource
  private StockService stockService;

  @Resource
  private RedisTemplate<String, String> redisTemplate;

  private static final String TEST_PRODUCT_ID = "redis_test_product_001";
  private static final String TEST_USER_ID = "redis_test_user_001";
  private static final String TEST_SCENE = "REDIS_SECKILL";

  @BeforeEach
  void setUp() {
    // 清理测试数据
    cleanupTestData();

    // 初始化测试商品库存
    stockService.initStock(TEST_PRODUCT_ID, 100);
  }

  @Test
  void testInitStock() {
    // 测试初始化库存
    String productId = "redis_init_product";
    Integer initialStock = 50;

    stockService.initStock(productId, initialStock);

    Integer currentStock = stockService.getCurrentStock(productId);
    assertEquals(initialStock, currentStock);

    // 清理
    redisTemplate.delete(StockConstant.STOCK_KEY_PREFIX + productId);
  }

  @Test
  void testGetCurrentStock() {
    // 测试获取当前库存
    Integer currentStock = stockService.getCurrentStock(TEST_PRODUCT_ID);
    assertEquals(100, currentStock);

    // 测试不存在的商品
    Integer nonExistStock = stockService.getCurrentStock("non_exist_product");
    assertNull(nonExistStock);
  }

  @Test
  void testDeductStockSuccess() {
    // 测试成功扣减库存
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(10)
        .userId(TEST_USER_ID)
        .scene(TEST_SCENE)
        .build();

    StockDeductResponse response = stockService.deductStock(param);

    assertTrue(response.getSuccess());
    assertEquals("库存扣减成功", response.getMessage());
    assertEquals(TEST_PRODUCT_ID, response.getProductId());
    assertEquals(10, response.getAmount());
    assertEquals(90, response.getRemainingStock());
    assertNotNull(response.getRecordId());

    // 验证库存确实被扣减
    Integer remainingStock = stockService.getCurrentStock(TEST_PRODUCT_ID);
    assertEquals(90, remainingStock);
  }

  @Test
  void testDeductStockInsufficientStock() {
    // 测试库存不足
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(150) // 超过库存
        .userId(TEST_USER_ID)
        .scene(TEST_SCENE)
        .build();

    StockDeductResponse response = stockService.deductStock(param);

    assertFalse(response.getSuccess());
    assertEquals("商品库存不足", response.getMessage());
    assertEquals(TEST_PRODUCT_ID, response.getProductId());
    assertEquals(150, response.getAmount());

    // 验证库存未被扣减
    Integer remainingStock = stockService.getCurrentStock(TEST_PRODUCT_ID);
    assertEquals(100, remainingStock);
  }

  @Test
  void testDeductStockNotExists() {
    // 测试商品不存在
    String nonExistProductId = "non_exist_product";
    StockDeductParam param = StockDeductParam.builder()
        .productId(nonExistProductId)
        .amount(10)
        .userId(TEST_USER_ID)
        .scene(TEST_SCENE)
        .build();

    StockDeductResponse response = stockService.deductStock(param);

    assertFalse(response.getSuccess());
    assertEquals("商品库存不存在", response.getMessage());
    assertEquals(nonExistProductId, response.getProductId());
  }

  @Test
  void testDeductStockInvalidAmount() {
    // 测试无效的扣减数量
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(0) // 无效数量
        .userId(TEST_USER_ID)
        .scene(TEST_SCENE)
        .build();

    StockDeductResponse response = stockService.deductStock(param);

    assertFalse(response.getSuccess());
    assertEquals("扣减数量必须大于0", response.getMessage());
  }

  @Test
  void testConcurrentDeductStock() throws InterruptedException {
    // 测试并发扣减库存
    int threadCount = 20;
    int deductAmountPerThread = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      executor.submit(() -> {
        try {
          StockDeductParam param = StockDeductParam.builder()
              .productId(TEST_PRODUCT_ID)
              .amount(deductAmountPerThread)
              .userId(TEST_USER_ID + "_" + threadIndex)
              .scene(TEST_SCENE)
              .build();

          StockDeductResponse response = stockService.deductStock(param);

          if (response.getSuccess()) {
            successCount.incrementAndGet();
          } else {
            failureCount.incrementAndGet();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    // 验证结果
    System.out.println("成功扣减次数: " + successCount.get());
    System.out.println("失败扣减次数: " + failureCount.get());

    // 最多只能成功扣减20次（100/5=20）
    assertTrue(successCount.get() <= 20);
    assertEquals(threadCount, successCount.get() + failureCount.get());

    // 验证最终库存
    Integer finalStock = stockService.getCurrentStock(TEST_PRODUCT_ID);
    assertEquals(100 - (successCount.get() * deductAmountPerThread), finalStock.intValue());
  }

  @Test
  void testStockRecordOperations() {
    // 测试流水记录相关操作（Redis部分）

    // 1. 扣减库存生成流水
    StockDeductParam param = StockDeductParam.builder()
        .productId(TEST_PRODUCT_ID)
        .amount(20)
        .userId(TEST_USER_ID)
        .orderId("REDIS_ORDER_001")
        .scene(TEST_SCENE)
        .extInfo("{\"source\":\"redis_test\"}")
        .build();

    StockDeductResponse response = stockService.deductStock(param);
    assertTrue(response.getSuccess());

    String recordId = response.getRecordId();
    assertNotNull(recordId);

    // 2. 查询流水记录（从Redis）
    var record = stockService.getStockRecord(TEST_PRODUCT_ID, recordId);
    assertNotNull(record);
    assertEquals(TEST_PRODUCT_ID, record.getProductId());
    assertEquals(20, record.getAmount());
    assertEquals(TEST_USER_ID, record.getUserId());
    assertEquals("REDIS_ORDER_001", record.getOrderId());
    assertEquals(TEST_SCENE, record.getScene());
    assertEquals(StockConstant.OperationType.DEDUCT, record.getOperationType());

    // 3. 查询流水记录ID列表
    var recordIds = stockService.getStockRecordIds(TEST_PRODUCT_ID);
    assertTrue(recordIds.contains(recordId));
  }

  @Test
  void testSetStockRecordsExpireOnOffline() {
    // 测试商品下架时设置流水过期时间

    // 先扣减几次库存生成流水
    for (int i = 0; i < 3; i++) {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(5)
          .userId(TEST_USER_ID + "_" + i)
          .scene(TEST_SCENE)
          .build();

      stockService.deductStock(param);
    }

    // 设置流水过期时间
    Integer updatedCount = stockService.setStockRecordsExpireOnOffline(TEST_PRODUCT_ID);
    assertEquals(3, updatedCount.intValue());
  }

  @Test
  void testBatchDeleteStockRecords() {
    // 测试批量删除流水记录

    // 先生成一些流水记录
    for (int i = 0; i < 3; i++) {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(2)
          .userId(TEST_USER_ID + "_" + i)
          .scene(TEST_SCENE)
          .build();

      stockService.deductStock(param);
    }

    // 获取所有流水记录ID
    var recordIds = stockService.getStockRecordIds(TEST_PRODUCT_ID);
    assertEquals(3, recordIds.size());

    // 批量删除前2条记录
    var deleteParam = com.xiaowang.cola.tools.param.StockRecordBatchDeleteParam.builder()
        .productId(TEST_PRODUCT_ID)
        .recordIds(recordIds.subList(0, 2))
        .build();

    Integer deletedCount = stockService.batchDeleteStockRecords(deleteParam);
    assertEquals(2, deletedCount.intValue());

    // 验证剩余记录数量
    var remainingRecordIds = stockService.getStockRecordIds(TEST_PRODUCT_ID);
    assertEquals(1, remainingRecordIds.size());
  }

  private void cleanupTestData() {
    // 清理测试相关的Redis数据
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
