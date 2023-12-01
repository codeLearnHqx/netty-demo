package com.hqx.nio.c2;

import com.hqx.nio.c1.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;


@Slf4j
public class Server {

    /**
     * 根据分割符来打印完整的消息
     */
    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                ByteBufferUtil.debugAll(target);
            }
        }
        source.compact(); // 将buffer中未读完的消息压缩（写模式）
    }

    public static void main(String[] args) throws IOException {
        // 1. 创建 selector，管理多个 channel
        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式

        // 2. 建立 selector 和 channel 的联系（注册）
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // key 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register: {}", sscKey);

        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法，没有事件发生，线程阻塞；有事件，线程恢复运行
            selector.select();
            // 4. 处理事件, selectedKeys方法 返回了所有发生的事件
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator(); // 获取迭代器
            // 遍历所有事件
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                if (key.isAcceptable()) { // accept 事件
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    // 将一个 byteBuffer 作为附件关联到 selectionKey 上
                    // 注册进 selector
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ); // 关注读事件
                    log.debug("{}", sc);
                } else if (key.isReadable()){ // read 事件
                    try {
                        log.debug("read...");
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 获取当前 key 的附件
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        // 如果时正常断开，将返回 -1
                        // 如果 channel 中的数据没有读完，还会再触发一次read事件
                        int read = channel.read(byteBuffer);
                        if (read == -1) {
                            log.debug("客户端手动关闭连接");
                            key.cancel();
                        } else {
                            // 打印buffer中的数据
                            split(byteBuffer);
                            // buffer 经过 compact 后，如果position == limit 证明 未找到
                            // 分割符 '\n'，即数据的长度大于 ByteBuffer 当前的容量
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                // 对当前 key 的附件 ByteBuffer 进行2倍扩容
                                ByteBuffer newBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                // 将原来 buffer 中的数据存到 扩容后的 buffer 中
                                byteBuffer.flip(); // 切换读
                                newBuffer.put(byteBuffer);
                                // 扩容后的buffer替换旧的，成为新的 buffer，即重新设置 附件
                                key.attach(newBuffer);
                            }
                            log.debug("容量： {}", byteBuffer.capacity());
                        }
                    }  catch (IOException e) {
                        // 客户端中断连接异常处理
                        e.printStackTrace();
                        // 因为客户端断开，因此需要将key取消（从keys中删除）并在注销selector中的channel
                        key.cancel();
                    }
                }
                // 从 Set<SelectionKey> 移除已经处理的键，防止出现其他问题
                keys.remove();
            }

        }

    }

}
