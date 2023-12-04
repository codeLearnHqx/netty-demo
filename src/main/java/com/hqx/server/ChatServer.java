package com.hqx.server;

import com.hqx.protocol.MessageCodecSharable;
import com.hqx.protocol.ProtocolFrameDecoder;
import com.hqx.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
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
        MessageCodecSharable MESSAGE_CODEC_HANDLER = new MessageCodecSharable();
        LoginRequestMessageHandler LOGIN_REQUEST_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_MESSAGE_HANDLER = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler GROUP_CREATE_HANDLER = new GroupCreateRequestMessageHandler();
        GroupChatRequestMessageHandler GROUP_CHAT_HANDLER = new GroupChatRequestMessageHandler();
        GroupJoinRequestMessageHandler GROUP_JOIN_HANDLER = new GroupJoinRequestMessageHandler();
        GroupMembersRequestMessageHandler GROUP_MEMBERS_HANDLER = new GroupMembersRequestMessageHandler();
        GroupQuitRequestMessageHandler GROUP_QUIT_HANDLER = new GroupQuitRequestMessageHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();

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
                    ch.pipeline().addLast(MESSAGE_CODEC_HANDLER);
                    // 读写空闲状态处理器
                    // 5s 内如果没有收到 channel 的数据，会触发一个 IdleState.READER_IDLE（IdleStateEvent） 事件
                    ch.pipeline().addLast(new IdleStateHandler(5, 0 ,0));
                    // 同时作为入站、出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent idleStateEvent =  ( IdleStateEvent ) evt;
                            // 触发读空闲事件
                            if (idleStateEvent.state().equals(IdleState.READER_IDLE)) {
                                log.debug("已经超过5s没有读到数据了");
                                ctx.channel().close();
                            }
                        }
                    });
                    // 自定义的登录处理器
                    ch.pipeline().addLast(LOGIN_REQUEST_HANDLER);
                    // 自定义的单人聊天消息处理器
                    ch.pipeline().addLast(CHAT_MESSAGE_HANDLER);
                    // 自定义的群创建请求处理器
                    ch.pipeline().addLast(GROUP_CREATE_HANDLER);
                    // 自定义的群聊处理器
                    ch.pipeline().addLast(GROUP_CHAT_HANDLER);

                    ch.pipeline().addLast(GROUP_JOIN_HANDLER);
                    ch.pipeline().addLast(GROUP_MEMBERS_HANDLER);
                    ch.pipeline().addLast(GROUP_QUIT_HANDLER);
                    // 自定义的客户端关闭处理器
                    ch.pipeline().addLast(QUIT_HANDLER);

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
