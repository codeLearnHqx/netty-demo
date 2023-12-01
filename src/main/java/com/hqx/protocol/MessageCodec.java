package com.hqx.protocol;

import com.hqx.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @Description 自定义的编解码器
 * @Create by hqx
 * @Date 2023/12/1 19:18
 */
public class MessageCodec extends ByteToMessageCodec<Message> {

    /**
     * 编码器
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 除去内容的字节数之外，协议固定写入的字节数为15字节，所以需要填充1无用个字节来使总数为2的整数倍
        // 1. 4字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节的版本
        out.writeByte(1);
        // 3. 1字节的序列化方式 jdk 0, json 1
        out.writeByte(0);
        // 4. 1字节的消息类型
        out.writeByte(msg.getMessageType());
        // 5. 4字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 大小为1的无用字节，用于对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos); // 将对象流写入 byte数组 流
        oos.writeObject(msg); // 将对象写入对象流
        byte[] bytes = bos.toByteArray(); // 获取内容字节数组
        // 7. 4字节，设置内容长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);

    }

    /**
     * 解码器
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取4字节（此缓冲区中将readerIndex增加4），魔数
        int magicNum = in.readInt();
        // 获取版本
        byte version = in.readByte();
        // 获取序列化方式

    }
}
