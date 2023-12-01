package com.hqx.nio.c1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/24 17:41
 */
public class TestByteBufferString {

    public static void main(String[] args) {
        // 1. 字符串转为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("你好".getBytes());
        debugAll(buffer);

        // 2. Charset
        ByteBuffer buffer1 = Charset.forName("utf-8").encode("hello");
        debugAll(buffer1);
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hellow");
        debugAll(buffer2);

        // 3. wrap
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        // #######################################################

        // ByteBuffer 转 String
        String str = StandardCharsets.UTF_8.decode(buffer3).toString();
        System.out.println(str);

    }

}
