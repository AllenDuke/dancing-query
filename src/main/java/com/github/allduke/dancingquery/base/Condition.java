package com.github.allduke.dancingquery.base;

import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author: allenduke
 * @mail: allenduke@163.com
 * @date 2021/12/28
 * description:
 */
@Getter
public class Condition implements Serializable {

    public static Integer CONNECT_TYPE_AND = 1;
    public static Integer CONNECT_TYPE_OR = 2;
    public static Integer CONNECT_TYPE_NOT = 3;

    public static Integer CONDITION_TYPE_EQ = 1;
    public static Integer CONDITION_TYPE_NOT_EQ = 2;
    public static Integer CONDITION_TYPE_IN = 3;
    public static Integer CONDITION_TYPE_NOT_IN = 4;
    public static Integer CONDITION_TYPE_LIKE = 5;
    public static Integer CONDITION_TYPE_NOT_LIKE = 6;

    /**
     * 与上一个条件的连接类型。
     */
    private Integer connectType;

    /**
     * 当前条件类型。
     */
    private Integer conditionType;

    private String fieldName;

    private Object val;

    public Condition(Integer connectType, Integer conditionType, Field field, Object val) {
        if (val instanceof Collection) {
            Collection<?> c = (Collection<?>) val;
        }
        this.connectType = connectType;
        this.conditionType = conditionType;
        this.fieldName = field.getName();
        this.val = val;
    }
}
