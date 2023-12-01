package com.hqx.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/28 23:14
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);// 固定大小的线程池
        // 2. 提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        // 3. 主线程通过 Future 获取结果
        log.debug("等待结果");
        Integer result = future.get(); // 同步等待，会阻塞当前线程
        log.debug("结果是: {}", result);
    }

}
