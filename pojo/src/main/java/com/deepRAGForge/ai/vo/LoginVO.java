package com.deepRAGForge.ai.vo;

import com.deepRAGForge.ai.po.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginVO {
    private User user;
    private String jwt;
}
