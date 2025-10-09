package com.xiaowang.cola.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wangjin
 */
@Data
@AllArgsConstructor
public class Student implements People {
    private String name;
}