package com.youxiu326.service.impl;

import com.youxiu326.entity.Account;
import com.youxiu326.entity.CartItem;
import com.youxiu326.entity.ShoppingCart;
import com.youxiu326.service.ShoopingCartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class ShoopingCartServiceImpl implements ShoopingCartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String getKey(HttpServletRequest req, HttpServletResponse resp, Account account) {
        //https://github.com/youxiu326/sb_shiro_session.git

        String key = null;  //最终返回的key
        String tempKey = ""; //用来存储cookie中的临时key,

        Cookie cartCookie = WebUtils.getCookie(req, "shoopingCart");
        if(cartCookie!=null){
            //获取Cookie中的key
            key = cartCookie.getValue();
            tempKey = cartCookie.getValue();
        }
        if(StringUtils.isBlank(key)){
            key = ShoppingCart.unLoginKeyPrefix + UUID.randomUUID();
            if (account!=null)
                key = ShoppingCart.loginKeyPrefix + account.getId();
            Cookie cookie = new Cookie("shoopingCart",key);
            cookie.setMaxAge(-1);
            cookie.setPath("/");
            resp.addCookie(cookie);
        }else if (StringUtils.isNotBlank(key) && account!=null){//⑵
            key = ShoppingCart.loginKeyPrefix + account.getId();
            if (tempKey.startsWith(ShoppingCart.unLoginKeyPrefix)){//⑴
                //1.满足cookie中取得的key 为未登录时的key
                //2.满足当前用户已经登录
                //3.合并未登录时用户所添加的购物车商品⑷
                mergeCart(tempKey,account);//⑶
            }
        }
        return key;
    }

    /**
     * 合并购物车
     * @param tempKey
     */
    public ShoppingCart  mergeCart(String tempKey,Account account){

        ShoppingCart loginCart = null;
        String loginkey = null;

        // 从redis取出用户缓存购物车数据
        HashOperations<String,String,ShoppingCart> vos = redisTemplate.opsForHash();
        ShoppingCart unLoginCart = vos.get("CACHE_SHOPPINGCART", tempKey);
        if (unLoginCart==null)
            unLoginCart = new ShoppingCart(tempKey);

        if (account != null && tempKey.startsWith(ShoppingCart.unLoginKeyPrefix)) {//⑵
            //如果用户登录 并且 当前是未登录的key
            loginkey = ShoppingCart.loginKeyPrefix + account.getId();
            loginCart = mergeCart(loginkey, account);
            if (null != unLoginCart.getCartItems()) {//⑴

                if (null != loginCart.getCartItems()) {
                    //满足未登录时的购物车不为空 并且 当前用户已经登录
                    //进行购物车合并
                    for (CartItem cv : unLoginCart.getCartItems()) {
                        long count = loginCart.getCartItems().stream().filter(it->it.getCode().equals(cv.getCode())).count();
                        if(count == 0 ){//没有重复的商品 则直接将商品加入购物车
                            loginCart.getCartItems().add(cv);
                        }else if(count == 1){//出现重复商品 修改数量
                            CartItem c = loginCart.getCartItems().stream().filter(it->it.getCode().equals(cv.getCode())).findFirst().orElse(null);
                            c.setQuantity(c.getQuantity()+1);
                        }
                    }
                } else {
                    //如果当前登录用户的购物车为空则 将未登录时的购物车合并
                    loginCart.setCartItems(unLoginCart.getCartItems());
                }
                //【删除临时key】
                vos.delete("CACHE_SHOPPINGCART",tempKey);
            }
        }else if(account!=null
                && tempKey.startsWith(ShoppingCart.loginKeyPrefix)
                && !(ShoppingCart.loginKeyPrefix+account.getId()).equals(tempKey)){
            //判断是否当前用户的缓存
            loginkey = ShoppingCart.loginKeyPrefix+account.getId();
            loginCart = mergeCart(loginkey,account);
        }

        //【将合并后的购物车数据 放入loginKey】
        vos.put("CACHE_SHOPPINGCART",loginkey, loginCart);

        return loginCart;
    }

    public String addCart(HttpServletRequest req, HttpServletResponse resp,Account account,CartItem item){
        String key = getKey(req, resp,account);
        ShoppingCart cacheData = mergeCart(key,account);

        return null;
    }


}