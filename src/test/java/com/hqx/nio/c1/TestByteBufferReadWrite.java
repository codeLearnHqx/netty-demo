package com.hqx.nio.c1;

import java.nio.ByteBuffer;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/24 15:34
 */
public class TestByteBufferReadWrite {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61); // 'a'
        debugAll(buffer);
        buffer.put(new byte[] {0x62, 0x63, 0x64, 0x65});
        debugAll(buffer);

        buffer.flip(); // 切换写
        System.out.println(buffer.get());
        debugAll(buffer);

        buffer.compact();  // 未读完的部分压缩，然后切换写模式
        debugAll(buffer);
        buffer.put(new byte[] {0x66, 0x67});
        debugAll(buffer);
    }

}
