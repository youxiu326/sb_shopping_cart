###### 未登录实现购物车功能


1.未登陆时如果添加购物车

2.用户登陆后如何合并未登录时添加的购物车


```

/**
     * 获得用户key
     *
     * 1.用户未登录情况下第一次进入购物车  -> 生成key 保存至cookie中
     * 2.用户未登录情况下第n进入购物车    -> 从cookie中取出key
     * 3.用户登录情况下                  -> 根据用户code生成key
     * 4.用户登录情况下并且cookie中存在key-> 从cookie取的的key从缓存取得购物车 合并至
     *  用户code生成key的购物车中去 ，这样后面才能根据用户code 取得正确的购物车
     *
     * @param req
     * @param resp
     * @param account
     * @return
     */
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
        String key = getKey(req, resp,account);//得到最终key
        ShoppingCart cacheCart = mergeCart(key,account);//根据key取得最终购物车对象
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


```

