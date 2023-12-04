package com.hqx.client;

import com.hqx.message.*;
import com.hqx.protocol.MessageCodecSharable;
import com.hqx.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description 客户端
 * @Create by hqx
 * @Date 2023/12/3 13:54
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        // loggingHandler可以被channel所共享
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1); // 计数为0才能向下运行
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 添加 handler
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    //ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC); // 出站、入站都会经过这个handler
                    // 读写空闲状态处理器
                    // 3s 内如果没有向 channel 的写入数据，就触发一个 IdleState.WRITER_IDLE（IdleStateEvent） 事件
                    ch.pipeline().addLast(new IdleStateHandler(0, 3 ,0));
                    // 同时作为入站、出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent idleStateEvent =  ( IdleStateEvent ) evt;
                            // 触发写空闲事件
                            if (idleStateEvent.state().equals(IdleState.WRITER_IDLE)) {
                                log.debug("已经超过3s没有写数据了，发送一个心跳包");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter(){
                        // channel触发的 read 事件，处理服务端返回的数据
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if (response.isSuccess()) { // 登录成功
                                    LOGIN.set(true);
                                }
                            }
                            // 唤醒 system in 线程
                            WAIT_FOR_LOGIN.countDown(); // 计数减1
                        }
                        // 连接建立后触发 active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 负责接收用户在控制台的输入，负责向服务端发送各种消息
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.print("请输入用户名：");
                                String username = scanner.nextLine();
                                System.out.print("请输入密码：");
                                String password = scanner.nextLine();
                                // 构造消息对象
                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                // 发送消息
                                ctx.writeAndFlush(message);

                                System.out.println("等待后续操作...");
                                try {
                                    WAIT_FOR_LOGIN.await(); // 阻塞，直到计数为0
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // 如果登录失败
                                if (!LOGIN.get()) {
                                    // 关闭 channel
                                    ctx.channel().close();
                                    return;
                                }
                                while (true) {
                                    System.out.println("=========================================");
                                    System.out.println("send [username] [content]"); // 单聊
                                    System.out.println("gsend [group name] [content]"); // 群聊
                                    System.out.println("gcreate [group name] [m1,m2,m3...]"); // 创建群聊
                                    System.out.println("gmembers [group name]"); // 查看群成员
                                    System.out.println("gjoin [group name]"); // 加入群
                                    System.out.println("gquit [group name]"); // 退群
                                    System.out.println("quit"); // 关闭客户端
                                    System.out.println("=========================================");
                                    String command = scanner.nextLine();
                                    String[] s = command.split(" ");
                                    switch (s[0]) {
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gcreate":
                                            // 聊天组的成员
                                            HashSet<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            set.add(username); // 加入自己
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1],set));
                                            break;
                                        case "gmembers":
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                            break;
                                        case "quit":
                                            // 关闭客户端
                                            ctx.channel().close();
                                            return;
                                    }
                                }

                            }, "system in").start();
                        }

                        // 连接断开时触发 inactive 事件（客户端强制中断不会触发）
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经断开...");
                        }

                        // 在出现异常时触发（客户端强制中断不会触发）
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("出现异常");
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync(); // 等待 NioSocketChannel 关闭

        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            // 关闭 事件循环组
            group.shutdownGracefully();
        }

    }
}
