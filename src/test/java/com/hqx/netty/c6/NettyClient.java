package com.hqx.netty.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/29 23:58
 */
@Slf4j
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(buf.toString(Charset.defaultCharset()));
                                // 释放ByteBuf
                                buf.release();
                                log.debug("read");
                            }
                        });
                    }
                })
                .connect("localhost", 8080)
                .sync()
                .channel();


        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                     group.shutdownGracefully();
            }
        });

        new Thread(() -> {
            log.debug("连接建立 {}", channel);
            Scanner scanner = new Scanner(System.in);
            while (true) {
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
}
