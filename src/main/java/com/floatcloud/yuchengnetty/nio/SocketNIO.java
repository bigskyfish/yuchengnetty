package com.floatcloud.yuchengnetty.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author floatcloud
 */
public class SocketNIO {



    public static void main(String[] args) throws IOException, InterruptedException {

        // 由于是非阻塞的，所以用于保存连接的SocketChannel
        List<SocketChannel> clients = new ArrayList<>(20);

        // 使用的java.nio包的类
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        // 绑定端口
        socketChannel.bind(new InetSocketAddress(8090));
        // 重点，设置Channel为非阻塞，默认为阻塞
        socketChannel.configureBlocking(false);
        for (;;){
            // 可以不加，这里为了避免五连接时刷新过快，便于演示
            Thread.sleep(1000);
            // accept方法在这里是非阻塞的（OS层次上为，socket 设置了 NONBLOCKING）
            SocketChannel accept = socketChannel.accept();
            if (accept == null) {
                System.out.println("无连接......");
            } else {
                System.out.printf("连接的端口为：%s \t", accept.socket().getPort());
                // IO 流程（非阻塞）
                accept.configureBlocking(false);
                clients.add(accept);
            }
            // 直接内存（零Copy）
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2048);
            // 遍历所有连接，写出数据
            Iterator<SocketChannel> iterator = clients.iterator();
            SocketChannel channel = null;
            while(iterator.hasNext()){
                channel = iterator.next();
                try {
                    int read = channel.read(byteBuffer);
                    if (read > 0){
                        byteBuffer.flip();
                        byte[] msg = new byte[byteBuffer.remaining()];
                        byteBuffer.get(msg);
                        String msgStr = new String(msg, Charset.forName("UTF_8"));
                        System.out.printf("端口为：%s；输出内容为：%s \t", channel.socket().getPort(),
                                msgStr);
                    } else if (read < 0){
                        // 客户端关闭
                        channel.close();
                        System.out.printf("端口%s的客户端关闭", channel.socket().getPort());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if(channel == null){
                        channel.close();
                    }
                }

            }
        }
    }

}
