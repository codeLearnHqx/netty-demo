package com.hqx.advence.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/30 17:46
 */
@Slf4j
public class HelloWorldServer {
    public static void main(String[] args) {
        // 用于建立连接
        NioEventLoopGroup boss = new NioEventLoopGroup();
        // 可以处理 IO 事件
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 创建服务器
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 调整系统缓冲区的大小，滑动窗口大小（针对服务器全局的）
            //serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
            // 调整netty的接收缓冲区（ByteBuf）（针对所有连接的）
            //serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
            serverBootstrap.group(boss, worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 设置数据包长度为8
                    //ch.pipeline().addLast(new FixedLengthFrameDecoder(8));
                    // 以换行符对数据进行解码，如果超过指定长度没有发现分割符则报错
                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    // 与上面的的解码器相同，但是可以指定分隔符
                    //ByteBuf buf = ch.alloc().buffer().writeBytes("*".getBytes());
                    //ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, buf));
                    // 打印日志
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            // 阻塞等待连接
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync(); // 阻塞等待channel关闭
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            // 关闭事件循环组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
