package com.hqx.server.handler;

import com.hqx.message.LoginRequestMessage;
import com.hqx.message.LoginResponseMessage;
import com.hqx.server.service.UserService;
import com.hqx.server.service.UserServiceFactory;
import com.hqx.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理登录请求的 handler （入站）
 * 只处理入站的 LoginRequestMessage 类型数据
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        // 调用业务方法
        UserService userService = UserServiceFactory.getUserService();
        boolean login = userService.login(username, password);
        LoginResponseMessage responseMessage;
        if (login) {
            // 保存连接信息（会话）
            SessionFactory.getSession().bind(ctx.channel(), username);
            responseMessage = new LoginResponseMessage(true, "登录成功");
        } else {
            responseMessage = new LoginResponseMessage(false, "用户名或密码不正确");
        }
        // 响应数据给客户端
        ctx.writeAndFlush(responseMessage);
    }
}
