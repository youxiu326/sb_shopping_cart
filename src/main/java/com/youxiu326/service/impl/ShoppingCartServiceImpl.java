package com.youxiu326.service.impl;

import com.youxiu326.common.JsonResult;
import com.youxiu326.entity.Account;
import com.youxiu326.entity.CartItem;
import com.youxiu326.entity.ShoppingCart;
import com.youxiu326.service.ShoppingCartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

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
     * 合并购物车 返回最终购物车
     * @param tempKey
     */
    public ShoppingCart  mergeCart(String tempKey,Account account) {

        ShoppingCart loginCart = null;
        String loginkey = null;

        // 从redis取出用户缓存购物车数据
        HashOperations<String, String, ShoppingCart> vos = redisTemplate.opsForHash();
        ShoppingCart unLoginCart = vos.get("CACHE_SHOPPINGCART", tempKey);
        if (unLoginCart == null){
            unLoginCart = new ShoppingCart(tempKey);
        }
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
                unLoginCart = loginCart;
                //【删除临时key】
                vos.delete("CACHE_SHOPPINGCART",tempKey);
                //【将合并后的购物车数据 放入loginKey】//TMP_4369f86d-c026-4b1b-8fec-f3c69f6ffac5
                vos.put("CACHE_SHOPPINGCART",loginkey, unLoginCart);
            }
        }else if(account!=null
                && tempKey.startsWith(ShoppingCart.loginKeyPrefix)
                && !(ShoppingCart.loginKeyPrefix+account.getId()).equals(tempKey)){
            //判断是否当前用户的缓存
            loginkey = ShoppingCart.loginKeyPrefix+account.getId();
            loginCart = mergeCart(loginkey,account);
            unLoginCart = loginCart;
            //【将合并后的购物车数据 放入loginKey】//TMP_4369f86d-c026-4b1b-8fec-f3c69f6ffac5
            vos.put("CACHE_SHOPPINGCART",loginkey, unLoginCart);
        }

        return unLoginCart;
    }

    /**
     * 添加购物车
     * @param req
     * @param resp
     * @param account 登陆用户信息
     * @param item  添加的购物车商品信息 包含商品code 商品加购数量
     * @return
     */
    public JsonResult addCart(HttpServletRequest req, HttpServletResponse resp,Account account,CartItem item){
        JsonResult result = new JsonResult();
        String key = getKey(req, resp,account);
        ShoppingCart cacheCart = mergeCart(key,account);
        if(StringUtils.isNotBlank(item.getCode()) && item.getQuantity()>0){
            //TODO 进行一系列 商品上架 商品code是否正确 最大购买数量....
            if(false){
                return result.error();
            }
            long count = 0;
            if(null != cacheCart.getCartItems()) {
                count = cacheCart.getCartItems().stream().filter(it->it.getCode().equals(item.getCode())).count();
            }
            if (count==0){
                //之前购物车无该商品记录 则直接添加
                cacheCart.getCartItems().add(item);
            }else {
                //否则将同一商品数量相加
                CartItem c = cacheCart.getCartItems().stream().filter(it->it.getCode().equals(item.getCode())).findFirst().orElse(null);
                c.setQuantity(c.getQuantity()+item.getQuantity());

            }
        }
        //【将合并后的购物车数据 放入loginKey】
        HashOperations<String,String,ShoppingCart> vos = redisTemplate.opsForHash();
        vos.put("CACHE_SHOPPINGCART",key, cacheCart);
        result.setData(cacheCart);
        return result;
    }

    /**
     * 移除购物车
     * @param req
     * @param resp
     * @param account
     * @param item
     * @return
     */
    public JsonResult removeCart(HttpServletRequest req, HttpServletResponse resp,Account account,CartItem item){
        JsonResult result = new JsonResult();
        String key = getKey(req, resp,account);
        ShoppingCart cacheCart =  mergeCart(key , account);//TODO 待探讨
        if(cacheCart!=null && cacheCart.getCartItems()!=null && cacheCart.getCartItems().size()>0){//⑴
            //
            long count = cacheCart.getCartItems().stream().filter(it->it.getCode().equals(item.getCode())).count();
            if(count == 1 ){//⑵
                CartItem ci = cacheCart.getCartItems().stream().filter(it->it.getCode().equals(item.getCode())).findFirst().orElse(null);
                if (ci.getQuantity()>item.getQuantity()){//⑶
                    ci.setQuantity(ci.getQuantity()-item.getQuantity());
                }else if(ci.getQuantity()<=item.getQuantity()){
                    cacheCart.getCartItems().remove(ci);
                }
                //1.满足缓存购物车中必须有商品才能减购物车
                //2.满足缓存购物车中有该商品才能减购物车
                //3.判断此次要减数量是否大于缓存购物车中数量 进行移除还是数量相减操作
            }
            HashOperations<String,String,ShoppingCart> vos = redisTemplate.opsForHash();
            vos.put("CACHE_SHOPPINGCART",key, cacheCart);
        }
        result.setData(cacheCart);
        return result;
    }

    /**
     *  【场景:我加购了一双40码的鞋子到购物车 现在我想换成41码的鞋子】
     *  【例如:原商品code ABCDEFG40   ->  ABCDEFG41】
     *
     * @param req
     * @param resp
     * @param account
     * @param item 新购物商品
     * @param oldItem 原购物商品
     * @return
     */
    public String updateCart(HttpServletRequest req, HttpServletResponse resp,Account account,CartItem item,CartItem oldItem){

        //TODO 校验商品信息是否合法 是否上架 库存 最大购买数量....
        if(false){
            return null;
        }

        String key = getKey(req, resp,account);
        ShoppingCart cacheCart =  mergeCart(key , account);//TODO 待探讨
        cacheCart.getCartItems().remove(item);
        cacheCart.getCartItems().remove(oldItem);
        cacheCart.getCartItems().add(oldItem);
        HashOperations<String,String,ShoppingCart> vos = redisTemplate.opsForHash();
        vos.put("CACHE_SHOPPINGCART",key, cacheCart);
        return null;
    }

}