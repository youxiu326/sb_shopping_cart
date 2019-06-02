package com.youxiu326.controller;

import com.youxiu326.common.JsonResult;
import com.youxiu326.entity.Account;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 登陆接口
 */
@Controller
public class LoginCtrl {


    @GetMapping("/")
    public String toLogin(HttpServletRequest req){
        return "login";
    }


    @PostMapping("/login")
    @ResponseBody
    public JsonResult login(HttpServletRequest req,Account account){

        JsonResult result = new JsonResult();

        if (StringUtils.isBlank(account.getCode()) || StringUtils.isBlank(account.getPwd())){
            result.error("账户或密码为空");
            return result;
        }
        //创建登陆用户账户 【主要逻辑是购物车 登陆用户id 就是用户code】
        account.setId(account.getCode());
        //将用户保存至session
        req.getSession().setAttribute("account",account);

        return result.success("登陆成功");
    }

    @PostMapping("/logout")
    @ResponseBody
    public JsonResult logout(HttpServletRequest req,Account account){

        JsonResult result = new JsonResult();

        if (StringUtils.isBlank(account.getCode())){
            result.error("账户为空");
            return result;
        }
        req.getSession().removeAttribute("account");

        return result.success("登出成功");
    }

}
