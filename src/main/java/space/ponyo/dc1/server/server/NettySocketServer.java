package space.ponyo.dc1.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import space.ponyo.dc1.server.util.LogUtil;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

public class NettySocketServer {
    private static final int PORT_DEVICE = 8000;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 关闭服务器方法
     */
    @PreDestroy
    public void close() {
        LogUtil.info("关闭服务器....");
        //优雅退出
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup) //绑定线程池
                    .channel(NioServerSocketChannel.class)// 指定使用的channel
                    .childHandler(new SocketChannelChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFutureDc1 = bootstrap.bind(PORT_DEVICE);
            channelFutureDc1.sync();//服务器异步创建绑定
            System.out.println("Server is listening：" + ((InetSocketAddress) channelFutureDc1.channel().localAddress()).getPort());
            channelFutureDc1.channel().closeFuture().sync();//关闭服务器
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放线程池资源
                workerGroup.shutdownGracefully().sync();
                bossGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {

        // 绑定客户端时触发的操作
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
//            pipeline.addLast(new DelimiterBasedFrameDecoder(1024 * 1024, Delimiters.lineDelimiter()));
            pipeline.addLast(new LineBasedFrameDecoder(1024 * 1024));
            pipeline.addLast(new IdleStateHandler(60, 60, 60));
            pipeline.addLast(new HeartBeatServerHandler());
            pipeline.addLast("handler", new ServerHandler());//服务器处理客户端请求
        }
    }

    public static class ServerHandler extends SimpleChannelInboundHandler {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ConnectionManager.getInstance().addChannel(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ConnectionManager.getInstance().removeChannel(ctx.channel());
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if ("".equals(msg)) {
                return;
            }
            ConnectionManager.getInstance().dispatchMsg(ctx.channel(), (String) msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
            super.channelReadComplete(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ConnectionManager.getInstance().removeChannel(ctx.channel());
            ConnectionManager.getInstance().clearInvalidData();
            ctx.close();
            super.exceptionCaught(ctx, cause);
        }
    }

    public static class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            LogUtil.notice("已经60秒未收到客户端的消息了！关闭客户端连接");
//            ctx.channel().close();
        }
    }

}
