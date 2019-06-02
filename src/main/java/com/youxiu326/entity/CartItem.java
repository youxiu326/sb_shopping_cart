package com.youxiu326.entity;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(code, cartItem.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}