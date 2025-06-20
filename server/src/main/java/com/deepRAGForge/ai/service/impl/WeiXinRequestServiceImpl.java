package com.deepRAGForge.ai.service.impl;

import com.deepRAGForge.ai.entity.WxSession;
import com.deepRAGForge.ai.service.WeiXinRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
public class WeiXinRequestServiceImpl implements WeiXinRequestService {
    @Value("${weixin.base-url}")
    private String baseUrl;
    @Value("${weixin.appid}")
    private String appId;
    @Value("${weixin.secret-key}")
    private String key;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
            .build();


    @Override
    public WxSession getSessionId(String code) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder()
                .addQueryParameter("appid", appId)
                .addQueryParameter("secret", key)
                .addQueryParameter("js_code", code)
                .addQueryParameter("grant_type", "authorization_code")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return objectMapper.readValue(response.body().string(),WxSession.class) ;
    }

    @Override
    public void authLogin() {

    }
}
