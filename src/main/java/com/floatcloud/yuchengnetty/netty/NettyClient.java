package com.floatcloud.yuchengnetty.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import sun.nio.ch.Net;

/**
 * @author floatcloud
 */
public class NettyClient {


    private void connect(String host, int port){
        // 配置客户端NIO线程组
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelHandler());
            // 发起异步连接请求 阻塞
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 等待客户端关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 退出
            loopGroup.shutdownGracefully();
        }
    }

    private class ChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new ClientHandler());
        }
    }

    public static void main(String[]gs) {
        new NettyClient().connect("127.0.0.1", 9090);
    }
}
