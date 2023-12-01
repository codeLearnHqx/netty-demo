package com.hqx.advence.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Random;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/30 17:56
 */
public class HelloWorldClient {
    public static void main(String[] args) {
        send();
    }
    private static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            // 创建服务器
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        // 会在连接 channel 建立成功后，会触发 active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer();
                            char c = 'a';
                            for (int i = 0; i < 10; i++) {
                                StringBuilder sb = makeStringEndWithBR(c);
                                c++;
                                System.out.println(sb);
                                buf.writeBytes(sb.toString().getBytes());
                            }
                            ctx.writeAndFlush(buf);
                            super.channelActive(ctx);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080);
            Channel channel = channelFuture.sync().channel();
            channel.closeFuture().sync(); // 阻塞等待channel关闭
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭事件循环组
            worker.shutdownGracefully();
        }
    }

    /**
     * 随机生成8位数长度以内的字母字符串，不满长度的用 _ 填充
     * @param c 起始 字符
     * @return 字符串
     */
    private static StringBuilder makeString(char c) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int j = 1; j <= r.nextInt(7) + 1; j++) {
            sb.append(c);
        }
        while (sb.length() < 8) {
            sb.append("_");
        }
        return sb;
    }

    private static StringBuilder makeStringEndWithBR(char c) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int j = 1; j <= r.nextInt(14) + 1; j++) {
            sb.append(c);
        }
        sb.append('\n');
        return sb;
    }

}
