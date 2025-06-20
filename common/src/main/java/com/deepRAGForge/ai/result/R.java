/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-01-19 21:22:11
 *  Class: com.learnWave.eduSynth.result.R
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.result;

import cn.hutool.json.JSONUtil;
import com.deepRAGForge.ai.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {


  private int code;
  private String msg;
  private T data;

  public R(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public static <T> R<T> success() {
    return new R<>(ErrorCode.SUCCESS.getCode(), null);
  }

 public static <T> R<T> success(String msg){
       return new R<>(ErrorCode.SUCCESS.getCode(), msg);
 }
  public static <T> R<T> success(T object) {
    return new R<>(ErrorCode.SUCCESS.getCode(), null, object);
  }
  public static <T> R<T> success(String msg, T object) {
      return new R<>(ErrorCode.SUCCESS.getCode(), msg, object);
}
  public static <T> R<T> error(ErrorCode errorCode) {
    return new R<>(errorCode.getCode(), errorCode.getMessage());
  }

  public static <T> R<T> error(int code, String msg) {
    return new R<>(code, msg);
  }


  public static R error(ErrorCode errorCode, String msg) {
    return new R(errorCode.getCode(), msg);
  }

  public static <T>R<T> error(ErrorCode errorCode,String msg,T data){
         return new R<>(errorCode.getCode(),msg,data);
  }
  /**
   * 重写toString方法, 默认返回json字符串
   *
   * @return
   */
  @Override
  public String toString() {
    return JSONUtil.toJsonStr(this);
  }

  /**
   * 转换为Map
   *
   * @return
   */
  public Map<String, Object> toMap() {
    return JSONUtil.toBean(JSONUtil.toJsonStr(this), Map.class);
  }
}
