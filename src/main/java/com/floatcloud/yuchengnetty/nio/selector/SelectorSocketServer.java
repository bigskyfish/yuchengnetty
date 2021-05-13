package com.floatcloud.yuchengnetty.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

/**
 * @author floatcloud
 */
public class SelectorSocketServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    public volatile boolean stop = false;




    /**
     * 初始化多路复用器，并绑定端口
     * @param port
     */
    public SelectorSocketServer(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口，并设置Socket非阻塞
            serverSocketChannel.bind(new InetSocketAddress(port), 1024).configureBlocking(false);
            // 将 Socket Server 注册到 Selector(事件驱动，事件为 SelectionKey.OP_ACCEPT)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.printf("服务启动...端口为：%s \t", port);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * 终止Socket服务
     */
    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while(!stop){
            try {
                // selector 中是否有连接注册
                if(selector.select(100) > 0){
                    // 获取所有 SelectionKey
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    SelectionKey key = null;
                    while(iterator.hasNext()){
                        key = iterator.next();
                        // 遍历得到后便移除该SelectionKey
                        iterator.remove();
                        // SelectionKey的处理方法
                        try {
                            handleSelectionKey(key);
                        } catch (IOException e){

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
     * 根据不同的 SelectionKey，选取不同的处理方式
     * selector  事件驱动
     * @param key 注册到selector的事件类型
     */
    private void handleSelectionKey(SelectionKey key) throws IOException {
       if (key.isValid()){
           if (key.isAcceptable()){
               // 获取 连接事件的Channel serverSocketChannel
               ServerSocketChannel channel = (ServerSocketChannel) key.channel();
               // 建立连接
               SocketChannel accept = channel.accept();
               // 连接通道设置非阻塞
               accept.configureBlocking(false);
               // 注册到selector,事件为 SelectionKey.OP_READ
               accept.register(selector, SelectionKey.OP_READ);
           } else if (key.isReadable()){
               // 读事件: SelectionKey.OP_READ
               SocketChannel socketChannel = (SocketChannel) key.channel();
               // ByteBuffer 缓存： 直接内存
               ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
               int read = socketChannel.read(byteBuffer);
               DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
               if (read > 0){
                   byteBuffer.flip();
                   byte[] bytes = new byte[byteBuffer.remaining()];
                   byteBuffer.get(bytes);
                   String body = new String(bytes, Charset.forName("UTF-8"));
                   String msg = LocalDateTime.now().format(dateTimeFormatter) + ":" + body;
                   System.out.println(msg);
                   // 写入逻辑
                   doWrite(socketChannel, msg);
               } else if(read < 0){
                   // 通道关闭
                   key.cancel();
                   socketChannel.close();
               }

           }
       }
    }


    /**
     * 向通道中写入信息
     * @param socketChannel 通道
     * @param msg 写入信息
     */
    private void doWrite(SocketChannel socketChannel, String msg) throws IOException {
        if (msg != null && !msg.trim().isEmpty()){
            byte[] bytes = msg.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        }

    }
}
