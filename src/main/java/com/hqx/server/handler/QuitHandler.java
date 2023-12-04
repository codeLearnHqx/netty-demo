package com.hqx.server.handler;

import com.hqx.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 客户端关闭处理器
 * @Create by hqx
 * @Date 2023/12/4 12:31
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {
    // 连接断开时触发 inactive
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 从会话管理器中移除 channel
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经断开", ctx.channel());
    }

    // 捕捉到异常时会触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 从会话管理器中移除 channel
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经异常断开，异常是 {}", ctx.channel(), cause.getMessage());
    }
}
