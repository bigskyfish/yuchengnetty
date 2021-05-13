package com.floatcloud.yuchengnetty.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author floatcloud
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncServerHandler> {


    @Override
    public void completed(AsynchronousSocketChannel result, AsyncServerHandler attachment) {
        attachment.socketChannel.accept(attachment, this);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        result.read(byteBuffer, byteBuffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncServerHandler attachment) {
        exc.printStackTrace();
        // 计数器执行countDown后，信号量Sage为0时，await才会执行下去
        attachment.countDownLatch.countDown();
    }
}
