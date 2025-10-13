package com.xiaowang.cola.tools.domain.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.xiaowang.cola.tools.constant.LuaScriptConstant;
import com.xiaowang.cola.tools.constant.StockConstant;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.param.StockDeductParam;
import com.xiaowang.cola.tools.param.StockRecordBatchDeleteParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 库存服务
 *
 * @author cola
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

  private final RedisTemplate<String, String> redisTemplate;
  private final StockRecordPersistenceService persistenceService;

  /**
   * 库存扣减
   *
   * @param param 扣减参数
   * @return 扣减响应
   */
  public StockDeductResponse deductStock(StockDeductParam param) {
    // 生成流水记录ID
    String recordId = UUID.randomUUID().toString(true);

    // 构建Redis keys
    String stockKey = buildStockKey(param.getProductId());
    String recordKey = buildRecordKey(param.getProductId(), recordId);
    String indexKey = buildRecordIndexKey(param.getProductId());

    // 构建流水记录
    StockRecord record = buildStockRecord(param, recordId);
    String recordJson = JSONUtil.toJsonStr(record);

    // 设置流水过期时间
    int expireTime = param.getRecordExpireTime() != null ? param.getRecordExpireTime()
        : StockConstant.DEFAULT_RECORD_EXPIRE_TIME;

    // 执行Lua脚本进行原子操作
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText(LuaScriptConstant.STOCK_DEDUCT_SCRIPT);
    script.setResultType(Long.class);

    Long result = redisTemplate.execute(
        script,
        Arrays.asList(stockKey, recordKey, indexKey),
        param.getAmount().toString(),
        recordId,
        recordJson,
        String.valueOf(expireTime));

    if (ObjectUtil.isEmpty(result)) {
      log.error("库存扣减Lua脚本执行异常，productId={}, amount={}",
          param.getProductId(), param.getAmount());
      return StockDeductResponse.failure(param.getProductId(), param.getAmount(), "系统异常，请稍后重试");
    }

    // 根据返回值处理结果
    StockDeductResponse response = handleDeductResult(result, param, recordId);

    // 如果扣减成功，异步持久化流水记录到数据库
    if (response.getSuccess()) {
      persistenceService.asyncPersistStockRecord(param.getProductId(), recordId);
    }

    return response;
  }

  /**
   * 初始化商品库存
   *
   * @param productId 商品ID
   * @param stock     初始库存
   */
  public void initStock(String productId, Integer stock) {
    String stockKey = buildStockKey(productId);
    redisTemplate.opsForValue().set(stockKey, stock.toString());
    log.info("初始化商品库存成功，productId={}, stock={}", productId, stock);
  }

  /**
   * 获取商品当前库存
   *
   * @param productId 商品ID
   * @return 当前库存，不存在返回null
   */
  public Integer getCurrentStock(String productId) {
    String stockKey = buildStockKey(productId);
    String stockStr = redisTemplate.opsForValue().get(stockKey);
    return stockStr != null ? Integer.valueOf(stockStr) : null;
  }

  /**
   * 批量删除流水记录（主动删除，用于对账后清理）
   *
   * @param param 批量删除参数
   * @return 删除的记录数量
   */
  public Integer batchDeleteStockRecords(StockRecordBatchDeleteParam param) {
    String indexKey = buildRecordIndexKey(param.getProductId());
    String recordIdsJson = JSONUtil.toJsonStr(param.getRecordIds());

    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText(LuaScriptConstant.BATCH_DELETE_STOCK_RECORDS_SCRIPT);
    script.setResultType(Long.class);

    Long deletedCount = redisTemplate.execute(
        script,
        Collections.singletonList(indexKey),
        recordIdsJson);

    log.info("批量删除流水记录完成，productId={}, 删除数量={}",
        param.getProductId(), deletedCount);

    return deletedCount != null ? deletedCount.intValue() : 0;
  }

  /**
   * 商品下架时设置流水过期时间（惰性删除）
   *
   * @param productId 商品ID
   * @return 设置过期时间的记录数量
   */
  public Integer setStockRecordsExpireOnOffline(String productId) {
    String indexKey = buildRecordIndexKey(productId);

    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText(LuaScriptConstant.SET_STOCK_RECORDS_EXPIRE_SCRIPT);
    script.setResultType(Long.class);

    Long updatedCount = redisTemplate.execute(
        script,
        Collections.singletonList(indexKey),
        String.valueOf(StockConstant.OFFLINE_RECORD_EXPIRE_TIME));

    log.info("商品下架设置流水过期时间完成，productId={}, 设置数量={}",
        productId, updatedCount);

    return updatedCount != null ? updatedCount.intValue() : 0;
  }

  /**
   * 获取商品的流水记录ID列表
   *
   * @param productId 商品ID
   * @return 流水记录ID列表
   */
  public List<String> getStockRecordIds(String productId) {
    String indexKey = buildRecordIndexKey(productId);
    return redisTemplate.opsForSet().members(indexKey).stream().toList();
  }

  /**
   * 根据记录ID获取流水详情
   *
   * @param productId 商品ID
   * @param recordId  记录ID
   * @return 流水记录，不存在返回null
   */
  public StockRecord getStockRecord(String productId, String recordId) {
    String recordKey = buildRecordKey(productId, recordId);
    String recordJson = redisTemplate.opsForValue().get(recordKey);

    if (recordJson != null) {
      return JSONUtil.toBean(recordJson, StockRecord.class);
    }
    return null;
  }

  /**
   * 处理扣减结果
   */
  private StockDeductResponse handleDeductResult(Long result, StockDeductParam param, String recordId) {
    if (StockConstant.LuaResult.SUCCESS.equals(result)) {
      // 扣减成功，获取剩余库存
      Integer remainingStock = getCurrentStock(param.getProductId());
      log.info("库存扣减成功，productId={}, amount={}, recordId={}, remainingStock={}",
          param.getProductId(), param.getAmount(), recordId, remainingStock);

      return StockDeductResponse.success(recordId, param.getProductId(), param.getAmount(), remainingStock);

    } else if (StockConstant.LuaResult.STOCK_NOT_EXISTS.equals(result)) {
      log.warn("库存不存在，productId={}, amount={}", param.getProductId(), param.getAmount());
      return StockDeductResponse.failure(param.getProductId(), param.getAmount(), "商品库存不存在");

    } else if (StockConstant.LuaResult.STOCK_INSUFFICIENT.equals(result)) {
      log.warn("库存不足，productId={}, amount={}", param.getProductId(), param.getAmount());
      return StockDeductResponse.failure(param.getProductId(), param.getAmount(), "商品库存不足");

    } else if (StockConstant.LuaResult.INVALID_AMOUNT.equals(result)) {
      log.warn("扣减数量无效，productId={}, amount={}", param.getProductId(), param.getAmount());
      return StockDeductResponse.failure(param.getProductId(), param.getAmount(), "扣减数量必须大于0");

    } else {
      log.error("未知的Lua脚本返回值，result={}, productId={}, amount={}",
          result, param.getProductId(), param.getAmount());
      return StockDeductResponse.failure(param.getProductId(), param.getAmount(), "系统异常，请稍后重试");
    }
  }

  /**
   * 构建流水记录
   */
  private StockRecord buildStockRecord(StockDeductParam param, String recordId) {
    return StockRecord.builder()
        .recordId(recordId)
        .productId(param.getProductId())
        .operationType(StockConstant.OperationType.DEDUCT)
        .amount(param.getAmount())
        .userId(param.getUserId())
        .orderId(param.getOrderId())
        .scene(param.getScene())
        .status(StockConstant.RecordStatus.PENDING)
        .extInfo(param.getExtInfo())
        .createTime(LocalDateTime.now())
        .build();
  }

  /**
   * 构建库存Key
   */
  private String buildStockKey(String productId) {
    return StockConstant.STOCK_KEY_PREFIX + productId;
  }

  /**
   * 构建流水记录Key
   */
  private String buildRecordKey(String productId, String recordId) {
    return StockConstant.STOCK_RECORD_KEY_PREFIX + productId + ":" + recordId;
  }

  /**
   * 构建流水索引Key
   */
  private String buildRecordIndexKey(String productId) {
    return StockConstant.STOCK_RECORD_INDEX_KEY_PREFIX + productId;
  }
}
