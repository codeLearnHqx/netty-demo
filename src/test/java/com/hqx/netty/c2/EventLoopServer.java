package com.hqx.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @Description NioEventLoop 处理 io 事件
 * @Create by hqx
 * @Date 2023/11/28 0:36
 */
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        // 进行进一步的细分
        EventLoopGroup group = new DefaultEventLoopGroup();
        new ServerBootstrap()
                // boss 和 worker
                // boss只负责 ServerSocketChannel 上的 accept 事件， worker 只负责 SocketChannel 上的读写
                // 即参数1的NioEventLoopGroup是用于处理 NioServerSocketChannel 的accept事件的，
                // 参数2的NioEventLoopGroup是处理 SocketChannel的 accept以外的所有事件的
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 添加入站处理器来自定义处理业务
                        // 将此 handler 交给 DefaultEventLoopGroup 的一个线程去执行，而不是交给 NioEventLoopGroup 中的一个线程去执行，以避免
                        // 该 handler 的执行时间过长，影响到同一个线程中其他 channel 的执行，从而提高事件处理的效率（为了避免影响IO线程）
                        ch.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter(){
                            // 关注处理读事件
                            @Override
                            public void channelRead(ChannelHandlerContext ctx,/*ByteBuf*/ Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                // 指定字符集并打印到控制台
                                log.debug(buf.toString(StandardCharsets.UTF_8));
                                // 将msg传递到下一个handler去处理
                                ctx.fireChannelRead(msg);
                            }
                        }).addLast(group, "handler2", new ChannelInboundHandlerAdapter(){
                            // 关注处理读事件
                            @Override
                            public void channelRead(ChannelHandlerContext ctx,/*ByteBuf*/ Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                // 指定字符集
                                log.debug(buf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .bind(8080);
    }

}
