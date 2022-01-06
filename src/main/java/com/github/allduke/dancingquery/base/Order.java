package com.github.allduke.dancingquery.base;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author: allenduke
 * @mail: allenduke@163.com
 * @date 2022/1/4
 * description:
 */
@Getter
public class Order implements Serializable {

    private String fieldName;

    private Boolean isAsc;

    public Order(String fieldName, Boolean isAsc) {
        this.fieldName = fieldName;
        this.isAsc = isAsc;
    }
}