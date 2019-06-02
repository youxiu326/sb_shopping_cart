package com.youxiu326.common;

public class JsonResult {

    private String code;
    private String message;
    private Object data;

    public JsonResult() {
        this.code = "200";
        this.message = "操作成功";
    }

    public JsonResult success(String message){
        this.code = "200";
        this.message = message;
        return this;
    }

    public JsonResult error(String message){
        this.code = "400";
        this.message = message;
        return this;
    }

    public JsonResult error(){
        this.code = "400";
        this.message = "操作失败";
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
