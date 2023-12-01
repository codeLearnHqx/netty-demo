package com.hqx.nio.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/26 1:26
 */
@Slf4j
public class MultiThreadServer {

    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 开启非阻塞
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        // 1. 创建固定数量worker
        log.debug("电脑CPU核心数：{}", Runtime.getRuntime().availableProcessors());
        Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }

        AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 清除已经处理的key
                if (key.isAcceptable()) { // 发生 accept 事件时
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false); // 开启非阻塞
                    log.debug("connect... {}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register... {}", sc.getRemoteAddress());
                    // round robin（轮询） worker
                    workers[index.getAndIncrement() % workers.length].register(sc); // 初始化 selector, 启动 worker0
                    log.debug("after register... {}", sc.getRemoteAddress());
                }
            }
        }


    }


    /**
     * 监测读写事件
     */
    static class Worker implements Runnable{
        private Thread thread;
        private Selector selector;
        private String name; // 线程名
        // 默认线程未初始化，该变量一修改其他的线程都能马上看到变化
        private volatile boolean start = false;
        // 创建任务队列，线程安全的队列，可以用在线程间传递数据
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name) {
            this.name = name;
        }

        /**
         * 初始化线程和selector
         */
        public void register(SocketChannel sc) throws IOException {
            if (!this.start) {
                this.thread = new Thread(this, this.name);
                this.selector = Selector.open();
                this.thread.start(); // 创建线程并调用线程中的 run 方法
                this.start = true;
            }
            // 往队列中添加任务
            this.queue.add(() -> {
                // 将boss中的 channel 注册进 worker 中的 selector 并关注 read 事件
                try {
                    sc.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // boss线程唤醒 worker 的selector
            this.selector.wakeup();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    // 没事件发生时阻塞，但可以被 wakeup() 唤醒
                    this.selector.select();
                    // 从队列中取出任务
                    Runnable task = this.queue.poll();
                    if (task != null) {
                        // 执行任务里面的代码
                        task.run(); // 这里不会创建线程，只是调用了 run 方法而已
                    }
                    // 获取所有事件 key
                    Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove(); // 从集合中移除当前已经处理的key
                        if (key.isReadable()) { // 可读
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel sc = (SocketChannel) key.channel();
                            log.debug("read...{}", sc.getRemoteAddress());
                            sc.read(buffer);
                            buffer.flip(); // 切换读
                            debugAll(buffer); // 打印buffer内容
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
