package com.hqx.protocol;

import com.hqx.config.Config;
import com.hqx.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @Description
 *  消息自定义的编解码器 （多个channel间可共享，所以只需要1个实例），必须保证接收ByteBuf是完整的，
 *  即必须配合 LengthFieldBasedFrameDecoder 帧解码器一起使用
 * @Create by hqx
 * @Date 2023/12/3 13:27
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 除去内容的字节数之外，协议固定写入的字节数为15字节，所以需要填充1无用个字节来使总数为2的整数倍
        // 1. 4字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节的版本
        out.writeByte(1);
        // 3. 1字节的序列化方式 jdk 0, json 1
        /*
            枚举对象可以通过ordinal()将枚举值转换成整数，按照枚举类中的枚举值顺序进行转换
         */
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4. 1字节的消息类型
        out.writeByte(msg.getMessageType());
        // 5. 4字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 大小为1的无用字节，用于对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7. 4字节，设置内容长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);

        // 填入消息集合，用于发送
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取4字节（此缓冲区中将readerIndex增加4），魔数
        int magicNum = in.readInt();
        // 获取版本
        byte version = in.readByte();
        // 获取序列化方式  0 或者 1
        byte serializerAlgorithm = in.readByte();
        // 获取消息类型
        byte messageType = in.readByte();
        // 获取消息序号
        int sequenceId = in.readInt();
        // 无用字节，不接收
        in.readByte();
        // 内容长度字节
        int length = in.readInt();
        // 获取内容字节
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 使用jdk反序列化，获取到内容对象    获取所有的枚举值数据组，然后根据协议中的序列化类型来进行反序列化
        // 找到指定的反序列算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithm];
        // 确定具体消息类型
        Class<?> messageClass = Message.getMessageClass(messageType);
        Object msg = algorithm.deserialize(messageClass, bytes);

        //log.debug("{} {} {} {} {} {}", magicNum, version, serializerType, messageType, sequenceId, length);
        //log.debug("{}", msg);

        // netty 约定解码出来的结果需要存到 out 里面，否则 handler 中拿不到数据
        out.add(msg);
    }
}
