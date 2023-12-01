package com.hqx.netty.c2;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description 事件循环
 * @Create by hqx
 * @Date 2023/11/27 14:38
 */
@Slf4j
public class TestEvenLoop {

    public static void main(String[] args) {

        // 1. 创建事件循环组
        // 如果没有指定线程数，netty 默认使用 cpu核心数 * 2 为线程数
        EventLoopGroup group = new NioEventLoopGroup(2);  // IO事件，普通任务，定时任务

        // 2. 获取下一个事件循环对象
        EventLoop eventLoop = group.next();

        // 3. 执行普通任务（异步）
        eventLoop.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("ok");
        });

        // 4. 执行定时任务
        eventLoop.scheduleWithFixedDelay(() -> {
            log.debug("ok");
        }, 0, 1, TimeUnit.SECONDS);

        log.debug("main");
    }

}
