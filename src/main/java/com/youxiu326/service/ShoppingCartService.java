package com.youxiu326.service;

import com.youxiu326.common.JsonResult;
import com.youxiu326.entity.Account;
import com.youxiu326.entity.CartItem;
import com.youxiu326.entity.ShoppingCart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ShoppingCartService {

    public String getKey(HttpServletRequest req, HttpServletResponse resp, Account account);

    public ShoppingCart mergeCart(String tempKey, Account account);

    public JsonResult addCart(HttpServletRequest req, HttpServletResponse resp, Account account, CartItem item);

    public JsonResult removeCart(HttpServletRequest req, HttpServletResponse resp, Account account, CartItem item);

    public String updateCart(HttpServletRequest req, HttpServletResponse resp,Account account,CartItem item,CartItem oldItem);

} 