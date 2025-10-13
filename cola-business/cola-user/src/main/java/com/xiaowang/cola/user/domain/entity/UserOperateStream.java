package com.xiaowang.cola.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaowang.cola.datasource.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <p>
 * 用户操作流水
 * </p>
 *
 * @author cola
 */
@Getter
@Setter
@TableName("user_operate_stream")
public class UserOperateStream extends BaseEntity {

  /**
   * 用户ID
   */
  private String userId;

  /**
   * 操作类型
   */
  private String type;

  /**
   * 操作时间
   */
  private Date operateTime;

  /**
   * 操作参数
   */
  private String param;

  /**
   * 扩展字段
   */
  private String extendInfo;

}
