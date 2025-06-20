package com.deepRAGForge.ai.utils;

import cn.hutool.core.util.IdUtil;

public class UsernameUtil {
    public static String getUsername(){
           return "user_"+ IdUtil.simpleUUID();
    }
}
