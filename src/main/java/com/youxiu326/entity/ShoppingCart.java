package com.youxiu326.entity;

import java.io.Serializable;
import java.util.List;

public class ShoppingCart implements Serializable {

    public static final String unLoginKeyPrefix="TMP_";

    public static final String loginKeyPrefix="USER_";

    private String key="";

    private List<CartItem> cartItems;

    public ShoppingCart(){}

    public ShoppingCart(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}