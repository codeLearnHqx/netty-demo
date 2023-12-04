package com.hqx.source;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/12/4 19:31
 */
@Slf4j
public class TestConnectionTimeout {
    public static void main(String[] args) {
        // 客户端通过 .option() 配置参数
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new LoggingHandler());
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080);
            channelFuture.sync().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("timeout");
        } finally {
            group.shutdownGracefully();
        }

    }
}
