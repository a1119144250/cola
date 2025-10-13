package com.xiaowang.cola.tools.constant;

/**
 * 库存相关常量
 *
 * @author cola
 */
public class StockConstant {

  /**
   * Redis Key前缀
   */
  public static final String STOCK_KEY_PREFIX = "stock:";
  public static final String STOCK_RECORD_KEY_PREFIX = "stock_record:";
  public static final String STOCK_RECORD_INDEX_KEY_PREFIX = "stock_record_index:";

  /**
   * 流水记录默认过期时间（7天）
   */
  public static final int DEFAULT_RECORD_EXPIRE_TIME = 7 * 24 * 60 * 60;

  /**
   * 商品下架后流水过期时间（24小时）
   */
  public static final int OFFLINE_RECORD_EXPIRE_TIME = 24 * 60 * 60;

  /**
   * Lua脚本返回值常量
   */
  public static final class LuaResult {
    /** 扣减成功 */
    public static final Long SUCCESS = 1L;
    /** 库存不存在 */
    public static final Long STOCK_NOT_EXISTS = 0L;
    /** 库存不足 */
    public static final Long STOCK_INSUFFICIENT = -1L;
    /** 扣减数量无效 */
    public static final Long INVALID_AMOUNT = -2L;
  }

  /**
   * 库存操作类型
   */
  public static final class OperationType {
    /** 扣减 */
    public static final String DEDUCT = "DEDUCT";
    /** 增加 */
    public static final String ADD = "ADD";
    /** 冻结 */
    public static final String FREEZE = "FREEZE";
    /** 解冻 */
    public static final String UNFREEZE = "UNFREEZE";
  }

  /**
   * 流水状态
   */
  public static final class RecordStatus {
    /** 待处理 */
    public static final String PENDING = "PENDING";
    /** 已完成 */
    public static final String COMPLETED = "COMPLETED";
    /** 已取消 */
    public static final String CANCELLED = "CANCELLED";
    /** 已对账 */
    public static final String RECONCILED = "RECONCILED";
  }
}
