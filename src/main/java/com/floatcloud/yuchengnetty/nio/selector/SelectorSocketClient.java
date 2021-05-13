package com.floatcloud.yuchengnetty.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author floatcloud
 */
public class SelectorSocketClient implements Runnable {

    private String host;

    private int port;

    public volatile boolean stop = false;

    private Selector selector;

    private SocketChannel socketChannel;



    public SelectorSocketClient(String host, int port){
        this.host = host == null || host.isEmpty() ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            // 设置 socketChannel 非阻塞
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        // 连接
        doConnect();
        // Selector 操作
        while (!stop){
            try {
                if(selector.select(100) > 0){
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        try {
                            handleInput(key);
                        } catch (IOException e) {
                            if(key != null){
                                key.cancel();
                                if(key.channel() != null){
                                    key.channel().close();
                                }
                            }
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // NPE 校验
        if (selector != null){
            try {
                // 多路复用器关闭：以为着与之关联的 Pipe 和 Channel 的资源也关闭，所以无需在进行 Pipe 和 Channel 的资源释放
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理 selector 中 事件
     * @param key 事件关键字
     */
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isConnectable()){
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.finishConnect()){
                // 已连接
                channel.register(selector, SelectionKey.OP_READ);
                doWrite(channel);
            } else {
                // 连接失败，进程退出
                System.exit(1);
            }
        } else if(key.isReadable()){
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            int read = channel.read(byteBuffer);
            if(read > 0) {
                byteBuffer.flip();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                String body = new String(bytes, Charset.forName("UTF-8"));
                System.out.printf("输出内容是%s \t", body);
                this.stop = true;
            } else if(read < 0) {
                key.cancel();
                channel.close();
            }

        }
    }

    /**
     * 连接 SocketServer
     */
    private void doConnect(){
        try {
            // 若 socketChannel 已连接到 SocketServer
            if(socketChannel.connect(new InetSocketAddress(host, port))){
                // 已连接 则 注册到 Selector 事件为 SelectionKey.OP_READ
                socketChannel.register(selector, SelectionKey.OP_READ);
                // IO 操作
                doWrite(socketChannel);
            } else {
                // 注册到 Selector， 连接事件 SelectionKey.OP_CONNECT
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 这里将键盘输入作为输入数据
     * @param socketChannel 通道
     */
    private void doWrite(SocketChannel socketChannel) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        byte[] bytes = msg.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if (!byteBuffer.hasRemaining()){
            System.out.println("Send server success!");
        }
    }
}
