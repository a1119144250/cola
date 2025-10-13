package com.xiaowang.cola.tools.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaowang.cola.tools.infrastructure.entity.StockRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水记录Mapper
 *
 * @author cola
 */
@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecordDO> {

  /**
   * 批量插入流水记录
   *
   * @param records 流水记录列表
   * @return 插入成功的记录数
   */
  int batchInsert(@Param("records") List<StockRecordDO> records);

  /**
   * 根据recordId列表批量更新状态为已对账
   *
   * @param recordIds     流水记录ID列表
   * @param reconcileTime 对账时间
   * @return 更新成功的记录数
   */
  int batchUpdateStatusToReconciled(@Param("recordIds") List<String> recordIds,
      @Param("reconcileTime") LocalDateTime reconcileTime);

  /**
   * 根据商品ID和状态查询流水记录
   *
   * @param productId 商品ID
   * @param status    状态
   * @return 流水记录列表
   */
  List<StockRecordDO> selectByProductIdAndStatus(@Param("productId") String productId,
      @Param("status") String status);

  /**
   * 根据商品ID和时间范围查询流水记录
   *
   * @param productId 商品ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 流水记录列表
   */
  List<StockRecordDO> selectByProductIdAndTimeRange(@Param("productId") String productId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * 删除已对账的流水记录（清理历史数据）
   *
   * @param beforeTime 指定时间之前的记录
   * @return 删除的记录数
   */
  int deleteReconciledRecordsBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
