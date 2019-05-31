package com.youxiu326.service;

import com.youxiu326.entity.Account;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ShoopingCartService {


    
    public String getKey(HttpServletRequest req, HttpServletResponse resp, Account account);

} 