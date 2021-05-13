package com.floatcloud.yuchengnetty.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO 异步IO模型
 * @author floatcloud
 */
public class AsyncServerHandler implements Runnable {

    private int port;

    /**
     * 发令枪（计数器）
     */
    CountDownLatch countDownLatch;

    AsynchronousServerSocketChannel socketChannel;

    public AsyncServerHandler(int port){
        this.port = port;
        try {
            socketChannel = AsynchronousServerSocketChannel.open();
            // 绑定端口
            socketChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        countDownLatch = new CountDownLatch(1);
        doAccept();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端连接
     */
    private void doAccept(){
        socketChannel.accept(this, new AcceptCompletionHandler());
    }
}
