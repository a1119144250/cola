package com.xiaowang.cola.tools.constant;

/**
 * Lua脚本常量
 *
 * @author cola
 */
public class LuaScriptConstant {

  /**
   * Token验证和删除脚本（原子操作）
   * KEYS[1]: token的完整key
   * ARGV[1]: 预期的token值
   * 返回值：1-验证成功并删除, 0-token不存在, -1-token不匹配
   */
  public static final String VALIDATE_AND_DELETE_TOKEN_SCRIPT = "local token = redis.call('get', KEYS[1])\n" +
      "if not token then\n" +
      "    return 0\n" +
      "end\n" +
      "if token == ARGV[1] then\n" +
      "    redis.call('del', KEYS[1])\n" +
      "    return 1\n" +
      "else\n" +
      "    return -1\n" +
      "end";

  /**
   * 库存扣减脚本（原子操作）
   * KEYS[1]: 库存key (stock:productId)
   * KEYS[2]: 流水key (stock_record:productId:recordId)
   * KEYS[3]: 流水索引key (stock_record_index:productId)
   * ARGV[1]: 扣减数量
   * ARGV[2]: 流水记录ID
   * ARGV[3]: 流水记录内容(JSON格式)
   * ARGV[4]: 流水过期时间(秒)
   * 
   * 返回值：
   * 1: 扣减成功
   * 0: 库存不存在
   * -1: 库存不足
   * -2: 扣减数量无效(<=0)
   */
  public static final String STOCK_DEDUCT_SCRIPT = "-- 参数校验\n" +
      "local deductAmount = tonumber(ARGV[1])\n" +
      "if not deductAmount or deductAmount <= 0 then\n" +
      "    return -2\n" +
      "end\n" +
      "\n" +
      "-- 获取当前库存\n" +
      "local currentStock = redis.call('get', KEYS[1])\n" +
      "if not currentStock then\n" +
      "    return 0\n" +
      "end\n" +
      "\n" +
      "-- 转换为数字并检查库存是否足够\n" +
      "currentStock = tonumber(currentStock)\n" +
      "if not currentStock or currentStock < deductAmount then\n" +
      "    return -1\n" +
      "end\n" +
      "\n" +
      "-- 扣减库存\n" +
      "local newStock = currentStock - deductAmount\n" +
      "redis.call('set', KEYS[1], newStock)\n" +
      "\n" +
      "-- 记录流水到Redis\n" +
      "local expireTime = tonumber(ARGV[4])\n" +
      "redis.call('setex', KEYS[2], expireTime, ARGV[3])\n" +
      "\n" +
      "-- 将流水ID添加到索引中，便于后续查询和清理\n" +
      "redis.call('sadd', KEYS[3], ARGV[2])\n" +
      "redis.call('expire', KEYS[3], expireTime)\n" +
      "\n" +
      "return 1";

  /**
   * 批量删除流水记录脚本
   * KEYS[1]: 流水索引key (stock_record_index:productId)
   * ARGV[1]: 流水记录ID数组(JSON格式)
   * 
   * 返回值：删除的记录数量
   */
  public static final String BATCH_DELETE_STOCK_RECORDS_SCRIPT = "local recordIds = cjson.decode(ARGV[1])\n" +
      "local deletedCount = 0\n" +
      "\n" +
      "for i, recordId in ipairs(recordIds) do\n" +
      "    local recordKey = 'stock_record:' .. string.match(KEYS[1], 'stock_record_index:(.+)') .. ':' .. recordId\n" +
      "    local deleted = redis.call('del', recordKey)\n" +
      "    if deleted == 1 then\n" +
      "        deletedCount = deletedCount + 1\n" +
      "        redis.call('srem', KEYS[1], recordId)\n" +
      "    end\n" +
      "end\n" +
      "\n" +
      "return deletedCount";

  /**
   * 商品下架时设置流水过期时间脚本
   * KEYS[1]: 流水索引key (stock_record_index:productId)
   * ARGV[1]: 过期时间(秒，24小时 = 86400)
   * 
   * 返回值：设置过期时间的记录数量
   */
  public static final String SET_STOCK_RECORDS_EXPIRE_SCRIPT = "local recordIds = redis.call('smembers', KEYS[1])\n" +
      "local expireTime = tonumber(ARGV[1])\n" +
      "local updatedCount = 0\n" +
      "\n" +
      "for i, recordId in ipairs(recordIds) do\n" +
      "    local recordKey = 'stock_record:' .. string.match(KEYS[1], 'stock_record_index:(.+)') .. ':' .. recordId\n" +
      "    local exists = redis.call('exists', recordKey)\n" +
      "    if exists == 1 then\n" +
      "        redis.call('expire', recordKey, expireTime)\n" +
      "        updatedCount = updatedCount + 1\n" +
      "    end\n" +
      "end\n" +
      "\n" +
      "-- 同时设置索引的过期时间\n" +
      "redis.call('expire', KEYS[1], expireTime)\n" +
      "\n" +
      "return updatedCount";
}





