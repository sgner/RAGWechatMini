/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-01-19 21:22:11
 *  Class: com.learnWave.eduSynth.properties.JWTProperties
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JWTProperties {
       private String secretKey;
       private long ttl;
       private String tokenName;
       private String adminSecretKey;
}
