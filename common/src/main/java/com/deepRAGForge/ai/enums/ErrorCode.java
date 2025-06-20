/*
 * Copyright © 2025
 * author: sgner-litianci
 * Last Modified: 2025-01-19 21:22:11
 *  Class: com.learnWave.eduSynth.enums.ErrorCode
 *  Project: LearnWave-EduSynth-System
 *  Repo: https://gitee.com/sgner/LearnWave-EduSynth-System.git
 */

package com.deepRAGForge.ai.enums;

public enum ErrorCode {

  SUCCESS(20000, "ok"),
  PARAMS_ERROR(40000, "请求参数错误"),
  UPLOAD_ERROR(40200, "上传文件失败"),
  NOT_LOGIN_ERROR(40100, "未登录"),
  NO_AUTH_ERROR(40101, "无权限"),
  NOT_FOUND_ERROR(40400, "请求数据不存在"),
  FORBIDDEN_ERROR(40300, "禁止访问"),
  SYSTEM_ERROR(50000, "系统内部异常"),
  OPERATION_ERROR(50001, "操作失败"),
  API_REQUEST_ERROR(50010, "接口调用失败");

  /**
   * 状态码
   */
  private final int code;

  /**
   * 信息
   */
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

}
