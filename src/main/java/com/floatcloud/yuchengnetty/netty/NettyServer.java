package com.floatcloud.yuchengnetty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author floatcloud
 */
public class NettyServer {

    public void bind(int port) throws InterruptedException {
        // 创建 服务端 NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());
            // 绑定端口,同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            // 等待服务器监听端口关闭
            future.channel().closeFuture().sync();
        } finally {
            // 退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            // DelimiterBasedFrameDecoder 分割符编码器，本例以#_为包信息的终止符
//             ByteBuf byteBuf = Unpooled.copiedBuffer("#_".getBytes());
//             DelimiterBasedFrameDecoder decoder = new DelimiterBasedFrameDecoder(1024, byteBuf);
//             socketChannel.pipeline().addLast(decoder);
            // FixedLengthFrameDecoder 定长解码器，解决TCP 粘包和拆包问题
            socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(100));
            socketChannel.pipeline().addLast(new StringDecoder());
            // IO Handler
            socketChannel.pipeline().addLast(new ServerHandler());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer().bind(9090);
    }
}
