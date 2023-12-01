package com.hqx.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @Description netty 服务器端
 * @Create by hqx
 * @Date 2023/11/26 17:20
 */
public class HelloServer {

    public static void main(String[] args) {
        // 1. 启动器，负责组装 netty 组件，启动服务器
        new ServerBootstrap()
                // 2. 相当于将 （selector + thread）加入到 group 中（可以监控 accept、read、write 等事件的发生）
                .group(new NioEventLoopGroup())
                // 3. 选择服务器的 ServerSocketChannel 实现
                .channel(NioServerSocketChannel.class)
                // 4. boss 负责处理连接 worker（child） 负责处理读写，决定了 worker（child）能执行哪些操作（handler）
                .childHandler(
                        // 5. NioSocketChannel 代表和客户端进行数据读写的通道，
                        // ChannelInitializer 是初始化器，可以添加 handler
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                // 6. 添加具体 handler
                                // 将 ByteBuf 解码成字符串
                                nioSocketChannel.pipeline().addLast(new StringDecoder());
                                // 自定义的 handler
                                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override // 读事件
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 打印处上一步转换好的字符串
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                // 7. 绑定监听端口
                .bind(8080);
    }

}
