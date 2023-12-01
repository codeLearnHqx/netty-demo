package com.hqx.advence.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * @Description http协议的编解码
 * @Create by hqx
 * @Date 2023/12/1 18:30
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 开启日志，显示更多的信息
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    // http 协议的编解码器
                    ch.pipeline().addLast(new HttpServerCodec());
                    // 只对上一个 handler 传递过来的指定类型的数据感兴趣 这里只关心 HttpRequest 类型的数据
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 获取请求的 uri
                            log.debug(msg.uri());

                            // 返回响应，会经过出栈处理器
                            DefaultFullHttpResponse response =
                                    // 参数1：协议版本，参数2：响应状态码
                                    new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            // 响应体写入数据
                            byte[] bytes = "<h1 style=\"color: red\">Hello, world</h1>".getBytes();
                            response.content().writeBytes(bytes);
                            // 响应头写入响应数据的长度，避免浏览器一直在等待接收数据
                            response.headers().setInt(CONTENT_LENGTH, bytes.length);
                            // channel 写入数据
                            ctx.writeAndFlush(response);
                        }
                    });

                    // 自定义业务处理器
                   /* ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override // 关注读
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("{}", msg.getClass());
                            if (msg instanceof HttpRequest) { // 请求行和请求头

                            } else if (msg instanceof HttpContent) { // 请求体

                            }
                            super.channelRead(ctx, msg);
                        }
                    });*/
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync(); // 阻塞等待NioServerSocketChannel关闭

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // NioServerSocketChannel关闭后关闭事件循环组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }


    }
}
