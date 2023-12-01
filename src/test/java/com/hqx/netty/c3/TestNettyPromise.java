package com.hqx.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/29 0:09
 */
@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2. 可以主动创建 promise，结果的容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        // 3. 任意一个线程执行计算，计算完毕后向 promise 填充结果
        new Thread(() -> {
            log.debug("开始计算...");
            try {
                int a = 10 / 0;
                Thread.sleep(1000);
                promise.setSuccess(100);
            } catch (Exception e) {
                // 将错误抛出到获取结果的线程
                promise.setFailure(e);
            }
            // 主动往 promise 中填充结果
        }).start();

        // 4. 接收结果的线程
        log.debug("等待结果...");
        log.debug("结果是: {}", promise.get()); // 这里获取的时候会出现异常

    }
}
