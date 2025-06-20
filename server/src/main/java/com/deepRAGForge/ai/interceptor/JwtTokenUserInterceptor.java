/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-02-12 17:22:49
 *  Class: com.learnWave.eduSynth.interceptor.JwtTokenUserInterceptor
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.interceptor;

import com.deepRAGForge.ai.properties.JWTProperties;
import com.deepRAGForge.ai.utils.JWTUtils;
import com.deepRAGForge.ai.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {
    @Autowired
    private JWTProperties jwtProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        token = token.substring(7);
        //验证token
        try {
            //从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken==null){
                //token已经失效了
                log.info("token失效");
                throw new RuntimeException();
            }
            Map<String, Object> claims = JWTUtils.parseJwt(jwtProperties.getSecretKey(),token);

            //把业务数据存储到ThreadLocal中
            ThreadLocalUtil.set(claims.get("userId"));
            //放行
            return true;
        } catch (Exception e) {
            //http响应状态码为401
            try{
                Map<String, Object> claims = JWTUtils.parseJwt(jwtProperties.getAdminSecretKey(),token);
                ThreadLocalUtil.set(claims);
            }catch (Exception ex){
                response.setStatus(401);
                return false;
            }
            //不放行
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
