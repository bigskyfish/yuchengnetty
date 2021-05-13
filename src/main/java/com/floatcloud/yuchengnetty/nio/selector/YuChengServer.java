package com.floatcloud.yuchengnetty.nio.selector;

/**
 * @author floatcloud
 */
public class YuChengServer {


    public static final int PORT = 8090;

    public static void main(String[] args) {
        SelectorSocketServer selectorSocketServer = new SelectorSocketServer(PORT);
        // 启动线程
        new Thread(selectorSocketServer, "YuCheng-Server-Thread-01").start();
    }
}
