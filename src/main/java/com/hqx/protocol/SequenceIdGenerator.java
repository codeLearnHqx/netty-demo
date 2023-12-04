package com.hqx.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 消息序号生成器
 * @Create by hqx
 * @Date 2023/12/4 23:30
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    /**
     *  获取自增的id
     */
    public static int nextId() {
        return id.getAndIncrement();
    }
}
