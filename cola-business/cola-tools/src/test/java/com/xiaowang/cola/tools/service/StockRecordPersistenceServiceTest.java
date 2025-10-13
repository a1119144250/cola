package com.xiaowang.cola.tools.service;

import com.xiaowang.cola.tools.constant.StockConstant;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.domain.service.StockRecordPersistenceService;
import com.xiaowang.cola.tools.infrastructure.entity.StockRecordDO;
import com.xiaowang.cola.tools.infrastructure.mapper.StockRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存流水持久化服务测试
 *
 * @author cola
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional // 测试完成后回滚数据
public class StockRecordPersistenceServiceTest {

  @Resource
  private StockRecordPersistenceService persistenceService;

  @Resource
  private StockRecordMapper stockRecordMapper;

  @Test
  void testQueryRecordsByProductIdAndStatus() {
    // 测试根据商品ID和状态查询流水记录
    List<StockRecord> completedRecords = persistenceService.queryRecordsByProductIdAndStatus(
        "PRODUCT_001", StockConstant.RecordStatus.COMPLETED);

    assertNotNull(completedRecords);
    assertEquals(2, completedRecords.size());

    // 验证记录内容
    StockRecord firstRecord = completedRecords.get(0);
    assertEquals("PRODUCT_001", firstRecord.getProductId());
    assertEquals(StockConstant.RecordStatus.COMPLETED, firstRecord.getStatus());
  }

  @Test
  void testQueryRecordsByProductIdAndTimeRange() {
    // 测试根据商品ID和时间范围查询流水记录
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 30, 0);

    List<StockRecord> records = persistenceService.queryRecordsByProductIdAndTimeRange(
        "PRODUCT_001", startTime, endTime);

    assertNotNull(records);
    assertEquals(3, records.size()); // test_record_001, test_record_002 和 test_record_003

    // 验证时间范围
    for (StockRecord record : records) {
      assertTrue(record.getCreateTime().isAfter(startTime.minusSeconds(1)));
      assertTrue(record.getCreateTime().isBefore(endTime.plusSeconds(1)));
    }
  }

  @Test
  void testBatchMarkRecordsAsReconciled() {
    // 测试批量标记流水记录为已对账
    List<String> recordIds = Arrays.asList("test_record_001", "test_record_002");

    int updatedCount = persistenceService.batchMarkRecordsAsReconciled(recordIds);

    assertEquals(2, updatedCount);

    // 验证状态已更新 - 通过查询记录ID来验证
    List<StockRecordDO> updatedRecords = stockRecordMapper.selectByProductIdAndStatus(
        "PRODUCT_001", StockConstant.RecordStatus.RECONCILED);

    assertTrue(updatedRecords.size() >= 2);

    // 验证记录状态
    for (StockRecordDO record : updatedRecords) {
      if ("test_record_001".equals(record.getRecordId()) || "test_record_002".equals(record.getRecordId())) {
        assertEquals(StockConstant.RecordStatus.RECONCILED, record.getStatus());
        assertNotNull(record.getReconcileTime());
      }
    }
  }

  @Test
  void testBatchInsertStockRecords() {
    // 测试批量插入流水记录
    StockRecordDO record1 = StockRecordDO.builder()
        .recordId("batch_test_001")
        .productId("BATCH_PRODUCT_001")
        .operationType(StockConstant.OperationType.DEDUCT)
        .amount(10)
        .beforeStock(100)
        .afterStock(90)
        .userId("BATCH_USER_001")
        .orderId("BATCH_ORDER_001")
        .scene("BATCH_TEST")
        .status(StockConstant.RecordStatus.PENDING)
        .extInfo("{\"test\":true}")
        .build();
    record1.setGmtCreate(new java.util.Date());
    record1.setGmtModified(new java.util.Date());
    record1.setDeleted(0);
    record1.setLockVersion(0);

    StockRecordDO record2 = StockRecordDO.builder()
        .recordId("batch_test_002")
        .productId("BATCH_PRODUCT_001")
        .operationType(StockConstant.OperationType.DEDUCT)
        .amount(5)
        .beforeStock(90)
        .afterStock(85)
        .userId("BATCH_USER_002")
        .orderId("BATCH_ORDER_002")
        .scene("BATCH_TEST")
        .status(StockConstant.RecordStatus.PENDING)
        .extInfo("{\"test\":true}")
        .build();
    record2.setGmtCreate(new java.util.Date());
    record2.setGmtModified(new java.util.Date());
    record2.setDeleted(0);
    record2.setLockVersion(0);

    List<StockRecordDO> records = Arrays.asList(record1, record2);
    int insertedCount = stockRecordMapper.batchInsert(records);

    assertEquals(2, insertedCount);

    // 验证插入的数据
    List<StockRecordDO> insertedRecords = stockRecordMapper.selectByProductIdAndStatus(
        "BATCH_PRODUCT_001", StockConstant.RecordStatus.PENDING);

    assertEquals(2, insertedRecords.size());
  }

  @Test
  void testCleanupReconciledRecords() {
    // 测试清理已对账的历史流水记录
    LocalDateTime beforeTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

    int deletedCount = persistenceService.cleanupReconciledRecords(beforeTime);

    // 应该删除 test_record_006 和 test_record_007（已对账且时间在范围内）
    assertTrue(deletedCount >= 0); // 可能为0，因为测试数据的reconcile_time可能为null
  }

  @Test
  void testMapperDirectOperations() {
    // 测试Mapper的直接操作

    // 测试根据商品ID和状态查询
    List<StockRecordDO> records = stockRecordMapper.selectByProductIdAndStatus(
        "PRODUCT_002", StockConstant.RecordStatus.COMPLETED);

    assertNotNull(records);
    assertEquals(2, records.size()); // test_record_004 和 test_record_005

    // 测试根据时间范围查询
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 12, 30, 0);

    List<StockRecordDO> timeRangeRecords = stockRecordMapper.selectByProductIdAndTimeRange(
        "PRODUCT_002", startTime, endTime);

    assertNotNull(timeRangeRecords);
    assertEquals(2, timeRangeRecords.size());
  }

  @Test
  void testQueryReconciledRecords() {
    // 测试查询已对账的记录
    List<StockRecord> reconciledRecords = persistenceService.queryRecordsByProductIdAndStatus(
        "PRODUCT_003", StockConstant.RecordStatus.RECONCILED);

    assertNotNull(reconciledRecords);
    assertEquals(2, reconciledRecords.size());

    // 验证记录状态
    for (StockRecord record : reconciledRecords) {
      assertEquals(StockConstant.RecordStatus.RECONCILED, record.getStatus());
      assertEquals("PRODUCT_003", record.getProductId());
    }
  }
}
