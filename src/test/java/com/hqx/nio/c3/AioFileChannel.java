package com.hqx.nio.c3;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;

/**
 * @Description 使用 AIO 读取文件
 * @Create by hqx
 * @Date 2023/11/26 16:39
 */

@Slf4j
public class AioFileChannel {

    public static void main(String[] args) {
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            /*
                参数1： ByteBuffer
                参数2： 读取的起始位置
                参数3： 附件（可以保存数据对象，未读完数据时）
                参数4： 回调对象 CompletionHandler
             */
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                /*
                 * read 成功
                 */
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...");
                    buffer.flip();
                    debugAll(buffer); // 打印数据
                }
                /*
                 * read 失败
                 */
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            log.debug("read end");
            /*
             * 因为读取文件开启的是守护线程，主线程结束后，守护线程也会结束
             * 所以会出现守护线程还没来的及开始打印就结束了的情况
             * 因此，让主线程休眠一点时间就可以看到结果
             */
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
