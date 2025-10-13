package com.xiaowang.cola.tools.controller;

import com.xiaowang.cola.base.response.SingleResponse;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.domain.service.StockService;
import com.xiaowang.cola.tools.domain.service.StockRecordCleanupService;
import com.xiaowang.cola.tools.param.StockDeductParam;
import com.xiaowang.cola.tools.param.StockRecordBatchDeleteParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 *
 * @author cola
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Validated
public class StockController {

  private final StockService stockService;
  private final StockRecordCleanupService cleanupService;

  /**
   * 库存扣减接口（秒杀场景）
   *
   * @param param 扣减参数
   * @return 扣减结果
   */
  @PostMapping("/deduct")
  public SingleResponse<StockDeductResponse> deductStock(@Valid @RequestBody StockDeductParam param) {
    log.info("收到库存扣减请求，productId={}, amount={}, userId={}, scene={}",
        param.getProductId(), param.getAmount(), param.getUserId(), param.getScene());

    try {
      StockDeductResponse response = stockService.deductStock(param);

      if (response.getSuccess()) {
        return SingleResponse.of(response);
      } else {
        return SingleResponse.fail("DEDUCT_FAILED", response.getMessage());
      }
    } catch (Exception e) {
      log.error("库存扣减异常，productId={}, amount={}", param.getProductId(), param.getAmount(), e);
      return SingleResponse.fail("SYSTEM_ERROR", "系统异常，请稍后重试");
    }
  }

  /**
   * 初始化商品库存
   *
   * @param productId 商品ID
   * @param stock     初始库存
   * @return 操作结果
   */
  @PostMapping("/init")
  public SingleResponse<String> initStock(@RequestParam @NotBlank(message = "商品ID不能为空") String productId,
      @RequestParam @Min(value = 0, message = "库存数量不能小于0") Integer stock) {
    log.info("初始化商品库存，productId={}, stock={}", productId, stock);

    try {
      stockService.initStock(productId, stock);
      return SingleResponse.of("库存初始化成功");
    } catch (Exception e) {
      log.error("库存初始化异常，productId={}, stock={}", productId, stock, e);
      return SingleResponse.fail("INIT_FAILED", "库存初始化失败");
    }
  }

  /**
   * 查询商品当前库存
   *
   * @param productId 商品ID
   * @return 当前库存
   */
  @GetMapping("/current/{productId}")
  public SingleResponse<Map<String, Object>> getCurrentStock(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId) {
    try {
      Integer currentStock = stockService.getCurrentStock(productId);

      Map<String, Object> result = new HashMap<>();
      result.put("productId", productId);
      result.put("currentStock", currentStock);
      result.put("exists", currentStock != null);

      return SingleResponse.of(result);
    } catch (Exception e) {
      log.error("查询库存异常，productId={}", productId, e);
      return SingleResponse.fail("QUERY_FAILED", "查询库存失败");
    }
  }

  /**
   * 查询商品流水记录ID列表
   *
   * @param productId 商品ID
   * @return 流水记录ID列表
   */
  @GetMapping("/records/{productId}")
  public SingleResponse<List<String>> getStockRecordIds(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId) {
    try {
      List<String> recordIds = stockService.getStockRecordIds(productId);
      return SingleResponse.of(recordIds);
    } catch (Exception e) {
      log.error("查询流水记录ID列表异常，productId={}", productId, e);
      return SingleResponse.fail("QUERY_FAILED", "查询流水记录失败");
    }
  }

  /**
   * 查询流水记录详情
   *
   * @param productId 商品ID
   * @param recordId  记录ID
   * @return 流水记录详情
   */
  @GetMapping("/record/{productId}/{recordId}")
  public SingleResponse<StockRecord> getStockRecord(@PathVariable @NotBlank(message = "商品ID不能为空") String productId,
      @PathVariable @NotBlank(message = "记录ID不能为空") String recordId) {
    try {
      StockRecord record = stockService.getStockRecord(productId, recordId);

      if (record != null) {
        return SingleResponse.of(record);
      } else {
        return SingleResponse.fail("NOT_FOUND", "流水记录不存在");
      }
    } catch (Exception e) {
      log.error("查询流水记录详情异常，productId={}, recordId={}", productId, recordId, e);
      return SingleResponse.fail("QUERY_FAILED", "查询流水记录失败");
    }
  }

  /**
   * 批量删除流水记录（对账后清理）
   *
   * @param param 批量删除参数
   * @return 删除结果
   */
  @PostMapping("/records/batch-delete")
  public SingleResponse<Map<String, Object>> batchDeleteStockRecords(
      @Valid @RequestBody StockRecordBatchDeleteParam param) {
    log.info("批量删除流水记录，productId={}, recordIds={}", param.getProductId(), param.getRecordIds());

    try {
      Integer deletedCount = stockService.batchDeleteStockRecords(param);

      Map<String, Object> result = new HashMap<>();
      result.put("productId", param.getProductId());
      result.put("requestCount", param.getRecordIds().size());
      result.put("deletedCount", deletedCount);

      return SingleResponse.of(result);
    } catch (Exception e) {
      log.error("批量删除流水记录异常，productId={}", param.getProductId(), e);
      return SingleResponse.fail("DELETE_FAILED", "批量删除流水记录失败");
    }
  }

  /**
   * 商品下架时设置流水过期时间
   *
   * @param productId 商品ID
   * @return 操作结果
   */
  @PostMapping("/offline/{productId}")
  public SingleResponse<Map<String, Object>> setStockRecordsExpireOnOffline(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId) {
    log.info("商品下架设置流水过期时间，productId={}", productId);

    try {
      Integer updatedCount = stockService.setStockRecordsExpireOnOffline(productId);

      Map<String, Object> result = new HashMap<>();
      result.put("productId", productId);
      result.put("updatedCount", updatedCount);
      result.put("expireTime", "24小时");

      return SingleResponse.of(result);
    } catch (Exception e) {
      log.error("设置流水过期时间异常，productId={}", productId, e);
      return SingleResponse.fail("EXPIRE_FAILED", "设置流水过期时间失败");
    }
  }

  /**
   * 批量标记流水记录为已对账
   *
   * @param recordIds 流水记录ID列表
   * @return 操作结果
   */
  @PostMapping("/records/mark-reconciled")
  public SingleResponse<Map<String, Object>> markRecordsAsReconciled(@RequestBody List<String> recordIds) {
    log.info("批量标记流水记录为已对账，数量={}", recordIds.size());

    try {
      int updatedCount = cleanupService.markRecordsAsReconciled(recordIds);

      Map<String, Object> result = new HashMap<>();
      result.put("requestCount", recordIds.size());
      result.put("updatedCount", updatedCount);

      return SingleResponse.of(result);
    } catch (Exception e) {
      log.error("批量标记流水记录为已对账异常", e);
      return SingleResponse.fail("RECONCILE_FAILED", "批量标记流水记录为已对账失败");
    }
  }

  /**
   * 商品下架处理接口
   *
   * @param productId 商品ID
   * @return 处理结果
   */
  @PostMapping("/product-offline/{productId}")
  public SingleResponse<Map<String, Object>> processProductOffline(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId) {
    log.info("处理商品下架，productId={}", productId);

    try {
      StockRecordCleanupService.ProcessResult processResult = cleanupService.processProductOffline(productId);

      Map<String, Object> result = new HashMap<>();
      result.put("productId", productId);
      result.put("persistedCount", processResult.getPersistedCount());
      result.put("expiredCount", processResult.getExpiredCount());
      result.put("message", "商品下架处理完成，流水记录已持久化并设置24小时过期时间");

      return SingleResponse.of(result);
    } catch (Exception e) {
      log.error("商品下架处理异常，productId={}", productId, e);
      return SingleResponse.fail("OFFLINE_FAILED", "商品下架处理失败");
    }
  }

  /**
   * 查询待对账的流水记录
   *
   * @param productId 商品ID
   * @return 待对账的流水记录列表
   */
  @GetMapping("/records/pending-reconcile/{productId}")
  public SingleResponse<List<StockRecord>> getPendingReconcileRecords(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId) {
    try {
      List<StockRecord> records = cleanupService.getPendingReconcileRecords(productId);
      return SingleResponse.of(records);
    } catch (Exception e) {
      log.error("查询待对账流水记录异常，productId={}", productId, e);
      return SingleResponse.fail("QUERY_FAILED", "查询待对账流水记录失败");
    }
  }

  /**
   * 根据时间范围查询流水记录（用于对账）
   *
   * @param productId 商品ID
   * @param startTime 开始时间（格式：yyyy-MM-ddTHH:mm:ss）
   * @param endTime   结束时间（格式：yyyy-MM-ddTHH:mm:ss）
   * @return 流水记录列表
   */
  @GetMapping("/records/time-range/{productId}")
  public SingleResponse<List<StockRecord>> getRecordsByTimeRange(
      @PathVariable @NotBlank(message = "商品ID不能为空") String productId,
      @RequestParam String startTime,
      @RequestParam String endTime) {
    try {
      LocalDateTime start = LocalDateTime.parse(startTime);
      LocalDateTime end = LocalDateTime.parse(endTime);

      List<StockRecord> records = cleanupService.getRecordsByTimeRange(productId, start, end);
      return SingleResponse.of(records);
    } catch (Exception e) {
      log.error("根据时间范围查询流水记录异常，productId={}, startTime={}, endTime={}",
          productId, startTime, endTime, e);
      return SingleResponse.fail("QUERY_FAILED", "根据时间范围查询流水记录失败");
    }
  }
}
