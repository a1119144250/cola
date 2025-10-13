package com.xiaowang.cola.tools.example;

import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.domain.service.StockService;
import com.xiaowang.cola.tools.domain.service.StockRecordCleanupService;
import com.xiaowang.cola.tools.param.StockDeductParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存扣减使用示例
 *
 * @author cola
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductExample {

  private final StockService stockService;
  private final StockRecordCleanupService cleanupService;

  /**
   * 秒杀场景示例
   */
  public void seckillExample() {
    String productId = "SECKILL_PRODUCT_001";
    String userId = "USER_12345";
    String orderId = "ORDER_67890";

    log.info("=== 秒杀场景示例开始 ===");

    try {
      // 1. 初始化商品库存（通常在商品上架时执行）
      stockService.initStock(productId, 1000);
      log.info("商品库存初始化完成，productId={}, stock=1000", productId);

      // 2. 用户参与秒杀，扣减库存
      StockDeductParam deductParam = StockDeductParam.builder()
          .productId(productId)
          .amount(1)
          .userId(userId)
          .orderId(orderId)
          .scene("SECKILL")
          .extInfo("{\"channel\":\"APP\",\"activity\":\"双11秒杀\"}")
          .build();

      StockDeductResponse response = stockService.deductStock(deductParam);

      if (response.getSuccess()) {
        log.info("库存扣减成功！recordId={}, 剩余库存={}",
            response.getRecordId(), response.getRemainingStock());

        // 3. 查询流水记录详情
        var record = stockService.getStockRecord(productId, response.getRecordId());
        log.info("流水记录详情：{}", record);

        // 4. 模拟订单支付成功，可以标记流水为已完成状态
        // 这里只是示例，实际业务中会在订单支付成功后调用
        log.info("订单支付成功，流水记录ID={}", response.getRecordId());

      } else {
        log.warn("库存扣减失败：{}", response.getMessage());
      }

      // 5. 查询当前库存
      Integer currentStock = stockService.getCurrentStock(productId);
      log.info("当前库存：{}", currentStock);

    } catch (Exception e) {
      log.error("秒杀场景示例执行异常", e);
    }

    log.info("=== 秒杀场景示例结束 ===");
  }

  /**
   * 并发扣减示例
   */
  public void concurrentDeductExample() {
    String productId = "CONCURRENT_PRODUCT_001";

    log.info("=== 并发扣减示例开始 ===");

    try {
      // 初始化库存
      stockService.initStock(productId, 10);
      log.info("商品库存初始化完成，productId={}, stock=10", productId);

      // 模拟多个用户同时扣减库存
      for (int i = 1; i <= 15; i++) {
        StockDeductParam param = StockDeductParam.builder()
            .productId(productId)
            .amount(1)
            .userId("USER_" + String.format("%03d", i))
            .scene("NORMAL_ORDER")
            .build();

        StockDeductResponse response = stockService.deductStock(param);

        if (response.getSuccess()) {
          log.info("用户{}扣减成功，剩余库存={}", param.getUserId(), response.getRemainingStock());
        } else {
          log.warn("用户{}扣减失败：{}", param.getUserId(), response.getMessage());
        }
      }

      // 查看最终库存
      Integer finalStock = stockService.getCurrentStock(productId);
      log.info("最终库存：{}", finalStock);

      // 查看流水记录
      List<String> recordIds = stockService.getStockRecordIds(productId);
      log.info("生成的流水记录数量：{}", recordIds.size());

    } catch (Exception e) {
      log.error("并发扣减示例执行异常", e);
    }

    log.info("=== 并发扣减示例结束 ===");
  }

  /**
   * 商品下架处理示例
   */
  public void productOfflineExample() {
    String productId = "OFFLINE_PRODUCT_001";

    log.info("=== 商品下架处理示例开始 ===");

    try {
      // 1. 初始化库存并进行一些扣减操作
      stockService.initStock(productId, 50);

      for (int i = 1; i <= 5; i++) {
        StockDeductParam param = StockDeductParam.builder()
            .productId(productId)
            .amount(2)
            .userId("USER_" + i)
            .orderId("ORDER_" + i)
            .scene("NORMAL_ORDER")
            .build();

        stockService.deductStock(param);
      }

      log.info("商品扣减操作完成，当前库存：{}", stockService.getCurrentStock(productId));

      // 2. 商品下架处理
      StockRecordCleanupService.ProcessResult result = cleanupService.processProductOffline(productId);

      log.info("商品下架处理完成，持久化记录数：{}，设置过期记录数：{}",
          result.getPersistedCount(), result.getExpiredCount());

      // 3. 查询待对账的流水记录
      var pendingRecords = cleanupService.getPendingReconcileRecords(productId);
      log.info("待对账的流水记录数量：{}", pendingRecords.size());

    } catch (Exception e) {
      log.error("商品下架处理示例执行异常", e);
    }

    log.info("=== 商品下架处理示例结束 ===");
  }

  /**
   * 对账流程示例
   */
  public void reconcileExample() {
    String productId = "RECONCILE_PRODUCT_001";

    log.info("=== 对账流程示例开始 ===");

    try {
      // 1. 模拟一些库存操作
      stockService.initStock(productId, 100);

      for (int i = 1; i <= 3; i++) {
        StockDeductParam param = StockDeductParam.builder()
            .productId(productId)
            .amount(5)
            .userId("USER_" + i)
            .orderId("ORDER_" + i)
            .scene("NORMAL_ORDER")
            .build();

        stockService.deductStock(param);
      }

      // 2. 查询待对账的流水记录
      var pendingRecords = cleanupService.getPendingReconcileRecords(productId);
      log.info("待对账的流水记录数量：{}", pendingRecords.size());

      // 3. 模拟对账完成，标记记录为已对账
      if (!pendingRecords.isEmpty()) {
        List<String> recordIds = pendingRecords.stream()
            .map(record -> record.getRecordId())
            .toList();

        int reconciledCount = cleanupService.markRecordsAsReconciled(recordIds);
        log.info("标记为已对账的记录数量：{}", reconciledCount);
      }

    } catch (Exception e) {
      log.error("对账流程示例执行异常", e);
    }

    log.info("=== 对账流程示例结束 ===");
  }

  /**
   * 运行所有示例
   */
  public void runAllExamples() {
    seckillExample();
    System.out.println();

    concurrentDeductExample();
    System.out.println();

    productOfflineExample();
    System.out.println();

    reconcileExample();
  }
}
