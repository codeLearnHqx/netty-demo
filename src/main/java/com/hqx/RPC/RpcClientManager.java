package com.hqx.RPC;

import com.hqx.RPC.handler.RpcResponseMessageHandler;
import com.hqx.RPC.service.HelloService;
import com.hqx.message.RpcRequestMessage;
import com.hqx.message.RpcResponseMessage;
import com.hqx.protocol.MessageCodecSharable;
import com.hqx.protocol.ProtocolFrameDecoder;
import com.hqx.protocol.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * RPC 客户端改进版
 */
@Slf4j
public class RpcClientManager {

    private static Channel channel = null;
    private static final Object LOCK = new Object();


    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("张三"));
        System.out.println(service.sayHello("李四"));
        System.out.println(service.sayHello("王五"));
    }

    /**
     * 创建代理类
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        // 定义代理类的类加载器
        ClassLoader classLoader = serviceClass.getClassLoader();
        // 代理类需要实现的接口数组
        Class<?>[] interfaces = new Class[]{serviceClass};
        // 创建代理对象 （每个方法的调用都会经过下面的内部类的流程）
        Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage message = new RpcRequestMessage(
                    sequenceId, // 序号
                    serviceClass.getName(), // 接口名称
                    method.getName(), // 方法名称
                    method.getReturnType(), // 方法返回值
                    method.getParameterTypes(), // 方法的形参类型
                    args // 实参
            );
            // 2. 将消息对象发送出去
            getChannel().writeAndFlush(message);
            // 3. 准备一个空的 promise 对象来接收结果             指定promise对象异步接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);
            // 4. 等待 promise 结果
            promise.await();
            if (promise.isSuccess()) {
                // 调用成功，获取结果并返回
                return promise.getNow();
            } else {
                // 调用失败，抛出异常信息
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) proxyInstance;
    }


    /**
     * <b>获取唯一的 channel 对象</b> <br/>
     * 单例模式双重检查锁实现并发安全（double check）
     */
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) {
            if (channel != null) {
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    /**
     * 初始化channel
     */
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // rpc 响应消息处理器
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);

            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                // 当客户端channel关闭后，优雅的关闭事件循环组
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}