/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-01-22 15:16:28
 *  Class: com.learnWave.eduSynth.util.JWTUtils
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JWTUtils {
    public static String createJWT(long ttl, String secretKey, Map<String,Object> claims){
        long expMillis = System.currentTimeMillis() + ttl;
        Date exp = new Date(expMillis);
        JwtBuilder builder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .setClaims(claims).setExpiration(exp);
        return builder.compact();
    }
    public static Claims parseJwt (String secretKey,String JWT){
       return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(JWT).getBody();
    }
    public static Claims checkJwt(String secretKey,String jwt){
         try{
             return parseJwt(secretKey, jwt);
         }catch (Exception e){
             log.error("jwt格式错误");
             return null;
         }
    }
}
