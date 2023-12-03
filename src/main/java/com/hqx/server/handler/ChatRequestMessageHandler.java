package com.hqx.server.handler;

import com.hqx.message.ChatRequestMessage;
import com.hqx.message.ChatResponseMessage;
import com.hqx.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description 聊天信息处理器，只关注处理 ChatRequestMessage 类型的数据
 * @Create by hqx
 * @Date 2023/12/3 17:55
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String to = msg.getTo(); // 发送目标
        Channel channel = SessionFactory.getSession().getChannel(to); // 获取目标对象的channel
        // 对方在线
        if (channel != null) {
            // 消息转发给指定对象
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        // 不在线
        else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方用户不在线或者不存在"));
        }

    }
}
