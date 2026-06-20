package com.xyzy.domain;

import com.xyzy.enums.AppHttpCodeEnum;

import java.io.Serializable;

public class ResponseResult<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public ResponseResult() {
        this.code = AppHttpCodeEnum.SUCCESS.getCode();
        this.msg = AppHttpCodeEnum.SUCCESS.getMsg();
    }

    public ResponseResult(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> ResponseResult<T> errorResult(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    public static <T> ResponseResult<T> okResult() {
        return new ResponseResult<>();
    }

    public static <T> ResponseResult<T> okResult(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    public static <T> ResponseResult<T> okResult(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setData(data);
        return result;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
