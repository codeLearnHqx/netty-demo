package com.hqx.protocol;

import com.hqx.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description 测试自定义编解码器的效果
 * @Create by hqx
 * @Date 2023/12/1 23:38
 */
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(
                        1024, 12, 4, 0, 0),
                new LoggingHandler(), // 日志打印
                new MessageCodec() // 自定义编解码器
        );


        // 1. 模拟消息出站 encode
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        //channel.writeOutbound(message);

        // 获取数据编码后的 ByteBuf
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
        // 2. 模拟消息入站 decode
        //channel.writeInbound(buf);

        // 3. 模拟在没有帧解码器的情况下出现 黏包、半包 现象
        ByteBuf s1 = buf.slice(0, 100); // 切片
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100); // 切片从索引100开始，到buf可读字节减100字节

        /*
            s1 最后会在 tail handler 被 release 成 0，从而被回收，因为 buf、s1、s2共用
            同一块内存，所以后面的 s2 内存被回收了。因此，分片 s1 需要引用加 1
         */
        s1.retain(); // 引用 加1，此时引用计数为 2

        // 模拟完整的消息，分成两部分进行发送
        channel.writeInbound(s1); // 写入后引用计数 1
        channel.writeInbound(s2); // 写入后引用计数 0
        // 最后 buf 缓冲区内存正好被回收

    }
}
