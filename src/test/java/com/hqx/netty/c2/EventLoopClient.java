package com.hqx.netty.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


/**
 * @Description Netty 客户端
 * @Create by hqx
 * @Date 2023/11/26 17:45
 */
@Slf4j
public class EventLoopClient {

    public static void main(String[] args) throws InterruptedException {
        // 2. 带有 Future，Promise 的类型都是和异步方法配套使用的，用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 1. 连接服务器
                // 异步非阻塞，main 线程发起了调用，真正执行 connect 的是 NioEventLoopGroup 中的线程
                .connect(new InetSocketAddress("localhost", 8080));
        // 2.1 使用 sync 方法同步处理结果
        /*channelFuture.sync(); // 阻塞住当前线程，直到nio线程连接建立完毕
        // 无阻塞获取 channel
        Channel channel = channelFuture.channel();
        log.debug("{}", channel);
        channel.writeAndFlush("hello");*/
        log.debug("main");
        // 2.2 使用 addListener(回调对象) 方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override // 在 nio 线程连接建立完毕后，会调用 operationComplete
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.debug("{}", channel);
                channel.writeAndFlush("hello");
            }
        });


    }

}
