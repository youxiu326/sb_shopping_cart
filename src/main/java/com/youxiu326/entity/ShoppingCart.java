package com.youxiu326.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车对象 一个购物车由n个CartItem组成
 */
public class ShoppingCart implements Serializable {

    public static final String unLoginKeyPrefix="TMP_";

    public static final String loginKeyPrefix="USER_";

    private String key="";

    private List<CartItem> cartItems = new ArrayList<>();//防止空指针

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