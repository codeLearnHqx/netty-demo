package com.hqx.nio.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/25 23:27
 */
@Slf4j
public class WriterServer {

    public static void main(String[] args) throws IOException {

        // 创建服务器通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 非阻塞
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        // channel 注册进 selector 的同时关注 accept 事件
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        // 监听端口
        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            // 阻塞线程，有事件发生时才运行
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // key 移除
                iterator.remove();
                if (key.isAcceptable()) {
                    // 接受连接，返回值为当前与客户端连接的通道
                    SocketChannel sc = ssc.accept();
                    // 设置非阻塞
                    sc.configureBlocking(false);
                    // 注册进 selector
                    log.debug("{}", key);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    log.debug("{}", scKey);
                    // 1. 向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30000000; i++) {
                        sb.append("a");
                    }

                    // 将数据传入 buffer
                    ByteBuffer buffer = Charset.defaultCharset().encode(CharBuffer.wrap(sb));

                    // 2. 返回值是实际写入的字节数
                    int write = sc.write(buffer);
                    System.out.println(write);

                    // 3. 判断buffer是否有剩余
                    if (buffer.hasRemaining()) {
                        // 4. 关注可写事件
                        // 在 buffer 中的数据为写入完时，在 key 原来的基础上再多关注写事件
                        // 因为管道的缓冲区有限，无法一直写入数据，所以让剩下的数据在缓冲区能写时再进行写入
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                        // 5. 将还未写完的数据保存在 key 的附件中
                        scKey.attach(buffer);
                    }
                } else if (key.isWritable()) { // 事件为可写时
                    SocketChannel sc = (SocketChannel) key.channel();
                    // 从SelectionKey的附件中获取上一次未写入完的数据，position会停在上次读的位置。
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    int write = sc.write(buffer);
                    System.out.println(write);
                    log.debug("{}", key);
                    // 6. 清理操作
                    if (!buffer.hasRemaining()) {
                        // 为了避免内存的浪费，将挂载在当前key上的 ByteBuffer 清除
                        key.attach(null);
                        // 不需要再关注可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);

                    }
                }

            }

        }



    }

}
