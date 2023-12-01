package com.hqx.nio.c1;

import java.nio.ByteBuffer;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/24 17:16
 */
public class TestByteBufferRead {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});

        buffer.flip(); // 切换读
        // 从头开始读
        buffer.get(new byte[4]);
        debugAll(buffer);

        buffer.rewind(); // 将position重置为0
        debugAll(buffer);
        System.out.println((char) buffer.get());
        debugAll(buffer);
        buffer.mark(); // 标记当前位置 position=1
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        debugAll(buffer);
        buffer.reset(); // 重置到mark（标记）位置
        System.out.println((char) buffer.get()); // b
    }

}
