package com.youxiu326.entity;

import java.io.InputStream;
import java.io.Serializable;

public class CartItem implements Serializable {

    private String code;

    private Integer quantity;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}