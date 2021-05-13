package com.floatcloud.yuchengnetty.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author floatcloud
 */
public class SocketServerDemo {

    private static final Integer PORT = 9200;

    /**
     * 线程池
     */
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10,
            50, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket( PORT);
        System.out.printf("socket 启动，端口：%s", PORT);
        while(true){
            // 死循环中执行连接和IO逻辑
            // 该accept过程为阻塞
            Socket accept = socket.accept();
            System.out.printf("socket accept 过程阻塞中，连接端口：%s", accept.getPort());
            threadPool.execute(()->{
                // 线程池执行IO逻辑
                Socket threadSocket = accept;
                try {
                    // IO 的过程也是阻塞的，因此需要另开一线程
                    // 防止都在主线程下，IO阻塞后，后续Socket连接无法获取到。
                    InputStream inputStream = threadSocket.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while (true){
                        System.out.println(bufferedReader.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }

    }


}
