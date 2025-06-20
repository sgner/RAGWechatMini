package com.deepRAGForge.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deepRAGForge.ai.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
