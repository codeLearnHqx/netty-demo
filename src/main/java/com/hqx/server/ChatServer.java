package com.hqx.server;

import com.hqx.protocol.MessageCodecSharable;
import com.hqx.protocol.ProtocolFrameDecoder;
import com.hqx.server.handler.ChatRequestMessageHandler;
import com.hqx.server.handler.LoginRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 聊天服务器
 * @Create by hqx
 * @Date 2023/12/3 12:53
 */
@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // handler
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoginRequestMessageHandler LOGIN_REQUEST_MESSAGE_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_REQUEST_MESSAGE_HANDLER = new ChatRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 基于长度字段的帧解码器
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    // 日志处理器
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 自定义的消息编解码器
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 自定义的登录处理器
                    ch.pipeline().addLast(LOGIN_REQUEST_MESSAGE_HANDLER);
                    // 自定义的聊天消息处理器
                    ch.pipeline().addLast(CHAT_REQUEST_MESSAGE_HANDLER);

                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync(); // 阻塞等待 NioServerSocketChannel 的关闭

        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
