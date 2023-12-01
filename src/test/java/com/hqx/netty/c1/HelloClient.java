package com.hqx.netty.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


/**
 * @Description Netty 客户端
 * @Create by hqx
 * @Date 2023/11/26 17:45
 */
public class HelloClient {

    public static void main(String[] args) throws InterruptedException {
        // 1. 启动器
        new Bootstrap()
                // 2. 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 3. 选择客户端 channel 实现
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    // 在连接后被调用
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 添加字符串编码器
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().channel().write(StandardCharsets.UTF_8.encode("hhhhhh"));
                    }
                })
                // 5. 连接到服务器
                .connect(new InetSocketAddress("localhost", 8080))
                .sync() // 阻塞方法，直到连接建立
                .channel() // 连接建立完，拿到 channel 对象
                // 6. 向服务器发送数据
                .writeAndFlush("hello world!");

    }

}
