package com.github.allduke.dancingquery.exception;

/**
 * @author: allenduke
 * @mail: allenduke@163.com
 * @date 2022/1/6
 * description:
 */
public class InvalidConditionException extends RuntimeException{

    public InvalidConditionException(String msg){
        super(msg);
    }

    public InvalidConditionException(Exception e){
        super(e);
    }
}
