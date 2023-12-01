package com.hqx.netty.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/30 16:10
 */
@Slf4j
public class Test {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder(Charset.defaultCharset()));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("接收到数据");
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(buf.toString(Charset.defaultCharset()));
                                super.channelRead(ctx, msg);
                            }
                        });
                        ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("写事件");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .connect("localhost", 8080);

        channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        Channel channel = future.channel();
                        channel.closeFuture().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                group.shutdownGracefully();
                            }
                        });
                        log.debug("连接建立 {}", channel);
                        channel.writeAndFlush("hello");
                        Scanner scanner = new Scanner(System.in);
                         /*
                            不要在当前线程直接使用 scanner 这种会阻塞线程的方法，
                            ，因为当前线程是 EventLoop 线程，
                            阻塞操作可能会导致事件循环（selector.select()）无法及时处理其他事件，
                            包括从服务器端接收的数据。
                         */
                        new Thread(() -> {
                            while (true) {
                                log.debug("所属线程");
                                String next = scanner.next();
                                if ("q".equals(next)) {
                                    // 请求关闭 channel
                                    channel.close();
                                    break;
                                }
                                channel.writeAndFlush(next);
                            }
                        }).start();
                    }
                });
    }
}
