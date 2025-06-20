package com.deepRAGForge.ai.service;

import com.deepRAGForge.ai.entity.WxSession;

import java.io.IOException;

public interface WeiXinRequestService {
    public WxSession getSessionId(String code) throws IOException;
    public void authLogin();
}
