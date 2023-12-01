package com.hqx.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/28 23:46
 */
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws InterruptedException {
        // 相当于线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 获取 group 中的线程（该线程会维护一个selector）
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000);
                log.debug("执行计算");
                return 70;
            }
        });
        // 通过异步回调获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("结果是 {}", future.getNow());
                group.shutdownGracefully();
            }
        });
        log.debug("main end");
    }

}
