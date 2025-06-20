package com.deepRAGForge.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepRAGForge.ai.mapper.UserMapper;
import com.deepRAGForge.ai.po.User;
import com.deepRAGForge.ai.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
