/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-01-19 21:22:11
 *  Class: com.learnWave.eduSynth.util.ThreadLocalUtil
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.utils;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    //提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }
	
    //存储键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }


    //清除ThreadLocal 防止内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
