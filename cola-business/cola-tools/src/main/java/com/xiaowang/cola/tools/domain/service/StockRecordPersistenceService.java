package com.xiaowang.cola.tools.domain.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.xiaowang.cola.tools.constant.StockConstant;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.infrastructure.entity.StockRecordDO;
import com.xiaowang.cola.tools.infrastructure.mapper.StockRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 库存流水持久化服务
 * 负责将Redis中的流水记录同步到数据库
 *
 * @author cola
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRecordPersistenceService {

  private final StockRecordMapper stockRecordMapper;
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 异步持久化单条流水记录到数据库
   *
   * @param productId 商品ID
   * @param recordId  流水记录ID
   */
  @Async
  public void asyncPersistStockRecord(String productId, String recordId) {
    try {
      // 从Redis获取流水记录
      String recordKey = buildRecordKey(productId, recordId);
      String recordJson = redisTemplate.opsForValue().get(recordKey);

      if (ObjectUtil.isEmpty(recordJson)) {
        log.warn("流水记录不存在，无法持久化，productId={}, recordId={}", productId, recordId);
        return;
      }

      // 解析JSON并转换为数据库实体
      StockRecord record = JSONUtil.toBean(recordJson, StockRecord.class);
      StockRecordDO recordDO = convertToRecordDO(record);

      // 插入数据库
      int result = stockRecordMapper.insert(recordDO);

      if (result > 0) {
        log.info("流水记录持久化成功，productId={}, recordId={}", productId, recordId);
      } else {
        log.error("流水记录持久化失败，productId={}, recordId={}", productId, recordId);
      }
    } catch (Exception e) {
      log.error("流水记录持久化异常，productId={}, recordId={}", productId, recordId, e);
    }
  }

  /**
   * 批量持久化商品的所有流水记录
   *
   * @param productId 商品ID
   * @return 持久化成功的记录数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchPersistStockRecords(String productId) {
    try {
      // 获取商品的所有流水记录ID
      String indexKey = buildRecordIndexKey(productId);
      Set<String> recordIds = redisTemplate.opsForSet().members(indexKey);

      if (ObjectUtil.isEmpty(recordIds)) {
        log.info("商品无流水记录需要持久化，productId={}", productId);
        return 0;
      }

      List<StockRecordDO> recordDOList = new ArrayList<>();

      // 批量获取流水记录
      for (String recordId : recordIds) {
        String recordKey = buildRecordKey(productId, recordId);
        String recordJson = redisTemplate.opsForValue().get(recordKey);

        if (ObjectUtil.isNotEmpty(recordJson)) {
          StockRecord record = JSONUtil.toBean(recordJson, StockRecord.class);
          StockRecordDO recordDO = convertToRecordDO(record);
          recordDOList.add(recordDO);
        }
      }

      if (recordDOList.isEmpty()) {
        log.warn("未找到有效的流水记录，productId={}", productId);
        return 0;
      }

      // 批量插入数据库
      int result = stockRecordMapper.batchInsert(recordDOList);

      log.info("批量持久化流水记录完成，productId={}, 成功数量={}", productId, result);
      return result;

    } catch (Exception e) {
      log.error("批量持久化流水记录异常，productId={}", productId, e);
      throw e;
    }
  }

  /**
   * 批量标记流水记录为已对账状态
   *
   * @param recordIds 流水记录ID列表
   * @return 更新成功的记录数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchMarkRecordsAsReconciled(List<String> recordIds) {
    if (ObjectUtil.isEmpty(recordIds)) {
      return 0;
    }

    try {
      int result = stockRecordMapper.batchUpdateStatusToReconciled(recordIds, LocalDateTime.now());
      log.info("批量标记流水记录为已对账，数量={}", result);
      return result;
    } catch (Exception e) {
      log.error("批量标记流水记录为已对账异常，recordIds={}", recordIds, e);
      throw e;
    }
  }

  /**
   * 根据商品ID和状态查询流水记录
   *
   * @param productId 商品ID
   * @param status    状态
   * @return 流水记录列表
   */
  public List<StockRecord> queryRecordsByProductIdAndStatus(String productId, String status) {
    try {
      List<StockRecordDO> recordDOList = stockRecordMapper.selectByProductIdAndStatus(productId, status);
      return recordDOList.stream()
          .map(this::convertToRecord)
          .toList();
    } catch (Exception e) {
      log.error("查询流水记录异常，productId={}, status={}", productId, status, e);
      return new ArrayList<>();
    }
  }

  /**
   * 根据商品ID和时间范围查询流水记录
   *
   * @param productId 商品ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 流水记录列表
   */
  public List<StockRecord> queryRecordsByProductIdAndTimeRange(String productId,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    try {
      List<StockRecordDO> recordDOList = stockRecordMapper.selectByProductIdAndTimeRange(
          productId, startTime, endTime);
      return recordDOList.stream()
          .map(this::convertToRecord)
          .toList();
    } catch (Exception e) {
      log.error("查询流水记录异常，productId={}, startTime={}, endTime={}",
          productId, startTime, endTime, e);
      return new ArrayList<>();
    }
  }

  /**
   * 清理已对账的历史流水记录
   *
   * @param beforeTime 指定时间之前的记录
   * @return 删除的记录数
   */
  @Transactional(rollbackFor = Exception.class)
  public int cleanupReconciledRecords(LocalDateTime beforeTime) {
    try {
      int result = stockRecordMapper.deleteReconciledRecordsBefore(beforeTime);
      log.info("清理已对账的历史流水记录完成，删除数量={}", result);
      return result;
    } catch (Exception e) {
      log.error("清理已对账的历史流水记录异常，beforeTime={}", beforeTime, e);
      throw e;
    }
  }

  /**
   * 转换为数据库实体
   */
  private StockRecordDO convertToRecordDO(StockRecord record) {
    StockRecordDO recordDO = new StockRecordDO();
    BeanUtil.copyProperties(record, recordDO);

    // 设置时间字段
    Date now = new Date();
    recordDO.setGmtCreate(now);
    recordDO.setGmtModified(now);
    recordDO.setDeleted(0);
    recordDO.setLockVersion(0);

    return recordDO;
  }

  /**
   * 转换为业务实体
   */
  private StockRecord convertToRecord(StockRecordDO recordDO) {
    StockRecord record = new StockRecord();
    BeanUtil.copyProperties(recordDO, record);

    // 手动转换时间字段
    if (recordDO.getGmtCreate() != null) {
      record.setCreateTime(recordDO.getGmtCreate().toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime());
    }
    if (recordDO.getGmtModified() != null) {
      record.setUpdateTime(recordDO.getGmtModified().toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime());
    }

    return record;
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
