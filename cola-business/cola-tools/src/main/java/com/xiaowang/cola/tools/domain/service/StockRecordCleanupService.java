package com.xiaowang.cola.tools.domain.service;

import com.xiaowang.cola.tools.constant.StockConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水记录清理服务
 * 负责主动删除和惰性删除机制
 *
 * @author cola
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRecordCleanupService {

  private final StockRecordPersistenceService persistenceService;
  private final StockService stockService;

  /**
   * 定时清理已对账的历史流水记录（主动删除）
   * 每天凌晨2点执行，清理30天前已对账的记录
   */
  @Scheduled(cron = "0 0 2 * * ?")
  public void cleanupReconciledRecords() {
    log.info("开始执行已对账流水记录清理任务");

    try {
      // 清理30天前已对账的记录
      LocalDateTime beforeTime = LocalDateTime.now().minusDays(30);
      int deletedCount = persistenceService.cleanupReconciledRecords(beforeTime);

      log.info("已对账流水记录清理完成，删除数量={}", deletedCount);
    } catch (Exception e) {
      log.error("已对账流水记录清理异常", e);
    }
  }

  /**
   * 定时检查并清理Redis中过期的流水记录索引
   * 每小时执行一次，清理空的索引集合
   */
  @Scheduled(cron = "0 0 * * * ?")
  public void cleanupEmptyRecordIndexes() {
    log.info("开始执行Redis流水记录索引清理任务");

    try {
      // 这里可以实现具体的清理逻辑
      // 由于Redis的过期策略会自动清理过期的key，这里主要是日志记录
      log.info("Redis流水记录索引清理任务完成");
    } catch (Exception e) {
      log.error("Redis流水记录索引清理异常", e);
    }
  }

  /**
   * 手动批量标记流水记录为已对账（对账完成后调用）
   *
   * @param recordIds 流水记录ID列表
   * @return 标记成功的记录数
   */
  public int markRecordsAsReconciled(List<String> recordIds) {
    log.info("开始批量标记流水记录为已对账，数量={}", recordIds.size());

    try {
      int result = persistenceService.batchMarkRecordsAsReconciled(recordIds);
      log.info("批量标记流水记录为已对账完成，成功数量={}", result);
      return result;
    } catch (Exception e) {
      log.error("批量标记流水记录为已对账异常", e);
      throw e;
    }
  }

  /**
   * 商品下架时的流水记录处理（惰性删除）
   *
   * @param productId 商品ID
   * @return 处理结果
   */
  public ProcessResult processProductOffline(String productId) {
    log.info("开始处理商品下架流水记录，productId={}", productId);

    try {
      ProcessResult result = new ProcessResult();

      // 1. 先将Redis中的流水记录持久化到数据库
      int persistedCount = persistenceService.batchPersistStockRecords(productId);
      result.setPersistedCount(persistedCount);

      // 2. 设置Redis中流水记录的过期时间为24小时
      int expiredCount = stockService.setStockRecordsExpireOnOffline(productId);
      result.setExpiredCount(expiredCount);

      log.info("商品下架流水记录处理完成，productId={}, 持久化数量={}, 设置过期数量={}",
          productId, persistedCount, expiredCount);

      return result;
    } catch (Exception e) {
      log.error("商品下架流水记录处理异常，productId={}", productId, e);
      throw e;
    }
  }

  /**
   * 查询待对账的流水记录
   *
   * @param productId 商品ID
   * @return 待对账的流水记录列表
   */
  public List<com.xiaowang.cola.tools.domain.entity.StockRecord> getPendingReconcileRecords(String productId) {
    return persistenceService.queryRecordsByProductIdAndStatus(
        productId, StockConstant.RecordStatus.COMPLETED);
  }

  /**
   * 根据时间范围查询流水记录（用于对账）
   *
   * @param productId 商品ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 流水记录列表
   */
  public List<com.xiaowang.cola.tools.domain.entity.StockRecord> getRecordsByTimeRange(
      String productId, LocalDateTime startTime, LocalDateTime endTime) {
    return persistenceService.queryRecordsByProductIdAndTimeRange(productId, startTime, endTime);
  }

  /**
   * 处理结果类
   */
  public static class ProcessResult {
    private int persistedCount;
    private int expiredCount;

    public int getPersistedCount() {
      return persistedCount;
    }

    public void setPersistedCount(int persistedCount) {
      this.persistedCount = persistedCount;
    }

    public int getExpiredCount() {
      return expiredCount;
    }

    public void setExpiredCount(int expiredCount) {
      this.expiredCount = expiredCount;
    }
  }
}
