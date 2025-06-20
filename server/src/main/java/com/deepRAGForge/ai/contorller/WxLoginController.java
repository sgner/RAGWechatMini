package com.deepRAGForge.ai.contorller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deepRAGForge.ai.entity.WxSession;
import com.deepRAGForge.ai.enums.ErrorCode;
import com.deepRAGForge.ai.po.User;
import com.deepRAGForge.ai.properties.JWTProperties;
import com.deepRAGForge.ai.result.R;
import com.deepRAGForge.ai.service.UserService;
import com.deepRAGForge.ai.service.WeiXinRequestService;
import com.deepRAGForge.ai.utils.JWTUtils;
import com.deepRAGForge.ai.utils.UsernameUtil;
import com.deepRAGForge.ai.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/wx/login")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.DELETE,RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT},allowCredentials = "true",originPatterns = "*")
public class WxLoginController {
    private final WeiXinRequestService weiXinRequestService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final JWTProperties jwtProperties;
    @GetMapping("/sessionId/{code}")
    public R getSessionID(@PathVariable String code) throws IOException {
        WxSession session = weiXinRequestService.getSessionId(code);
        System.out.println(session);
        if(session.getSession_key() == null || session.getOpenid() == null){
            return R.error(ErrorCode.NO_AUTH_ERROR,"异常账户");
        }
        User user = null;
        user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, session.getOpenid()));
        if(user==null){
            user = User.builder()
                    .createAt(LocalDateTime.now().toString())
                    .openId(session.getOpenid())
                    .username(UsernameUtil.getUsername())
                    .build();
            userService.save(user);
        }
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username",user.getUsername());
        userMap.put("userId",user.getId());
        String jwt = JWTUtils.createJWT(jwtProperties.getTtl(), jwtProperties.getSecretKey(), userMap);
        redisTemplate.opsForValue().set(jwt,jwt);
        return R.success(LoginVO.builder().jwt(jwt).user(user).build());
    }
}
