package com.floatcloud.yuchengnetty.aio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author floatcloud
 */
public class AsyncYuChengServer {

    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( 10,
            100, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    public static void main(String[] args) {
        threadPoolExecutor.execute(new AsyncServerHandler(9090));
    }

}
