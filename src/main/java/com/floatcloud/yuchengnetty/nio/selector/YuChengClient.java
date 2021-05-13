package com.floatcloud.yuchengnetty.nio.selector;

/**
 * 客户端
 * @author floatcloud
 */
public class YuChengClient {

    public static void main(String[] args) {
        // 此处可以使用线程池
        new Thread(new SelectorSocketClient("127.0.0.1", 8090), "Client-01").start();

    }

}
