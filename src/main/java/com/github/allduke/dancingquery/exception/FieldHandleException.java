package com.github.allduke.dancingquery.exception;

public class FieldHandleException extends RuntimeException {

    public FieldHandleException(Exception e) {
        super(e);
    }

    public FieldHandleException(String msg) {
        super(msg);
    }
}
