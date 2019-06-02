package com.youxiu326.controller;

import com.youxiu326.common.JsonResult;
import com.youxiu326.entity.Account;
import com.youxiu326.entity.CartItem;
import com.youxiu326.entity.ShoppingCart;
import com.youxiu326.service.ShoppingCartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 购物车 Controller
 */
@Controller
@RequestMapping("/shopping")
public class ShoppingCartCtrl {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Account account;

    @Autowired
    private ShoppingCartService service;


    /**
     * 进入首页
     * @return
     */
    @GetMapping("/index")
    public String toIndex(HttpServletRequest req, HttpServletResponse resp, Model model){

        account = (Account) req.getSession().getAttribute("account");

        String key = service.getKey(req, resp, this.account);
        ShoppingCart cacheCart = service.mergeCart(key, this.account);
        model.addAttribute("cartItems",cacheCart.getCartItems());

        return "index";
    }

    @PostMapping("/add")
    @ResponseBody
    public JsonResult add(HttpServletRequest req, HttpServletResponse resp, CartItem cartItem){
        account = (Account) req.getSession().getAttribute("account");
        JsonResult result = service.addCart(req, resp, account, cartItem);
        return result;
    }

    @PostMapping("/remove")
    @ResponseBody
    public JsonResult remove(HttpServletRequest req, HttpServletResponse resp, CartItem cartItem){
        account = (Account) req.getSession().getAttribute("account");
        JsonResult result = service.removeCart(req, resp, account, cartItem);
        return result;
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(HttpServletRequest req, HttpServletResponse resp){

        account = (Account) req.getSession().getAttribute("account");

        return "";
    }

}