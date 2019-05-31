package com.youxiu326.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 购物车 Controller
 */
@Controller
@RequestMapping("/shooping")
public class ShoopingCartCtrl {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/")
    public String toIndex(){

        return "index";
    }

    @PostMapping("/add")
    public String add(HttpServletRequest req, HttpServletResponse resp){



        return "";
    }

}