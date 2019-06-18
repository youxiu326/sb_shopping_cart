package com.youxiu326.entity;

import java.io.Serializable;

/**
 * 用户
 */
public class Account implements Serializable {

    private String id = "youxiu326";

    private String code = "test";

    private String pwd = "123456";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}