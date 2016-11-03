package io.cluster.http;

import io.cluster.http.core.ControllerManager;
import io.cluster.util.ServerConfigAutoLoader;
import io.cluster.util.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ANTS HTTP Log server. based on
 * https://github.com/netty/netty/tree/master/example/src/main/java/io/netty/example/http/snoop
 */
public class HttpLogServer extends Thread {

    static final Logger LOGGER = LogManager.getLogger(HttpLogServer.class.getName());
    int port;
    String ip;

    public HttpLogServer() {
        ip = "127.0.0.1";
        port = 9369;
        try {
            ip = ServerConfigAutoLoader.getConfigByName("HTTP.HOST");
            port = StringUtil.safeParseInt(ServerConfigAutoLoader.getConfigByName("HTTP.PORT"));
            ControllerManager.initialize();
        } catch (Exception ex) {
            LOGGER.error("Could not get config from file, get default config.", ex);
        }
    }

    @Override
    public void run() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel ch = b.bind(ip, port).sync().channel();
            ch.config().setConnectTimeoutMillis(30000);
            LOGGER.info("HttpLogServer is started and listening at " + this.ip + ":" + this.port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Could not init HttpLogServer: ", e);
            System.exit(1);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

//    public static void main(String[] args) {
//        try {
//            HttpServerConfigs configs;
//            int customPort = 0;
//            if (args.length == 1) {
//                CommonUtil.setBaseConfig(args[0]);
//                configs = HttpServerConfigs.load(CommonUtil.getHttpServerConfigFile());
//            } else if (args.length == 2) {
//                CommonUtil.setBaseConfig(args[0]);
//                configs = HttpServerConfigs.load(CommonUtil.getHttpServerConfigFile());
//                customPort = Integer.parseInt(args[1]);
//            } else {
//                configs = HttpServerConfigs.load();
//            }
//            int port = configs.getPort();
//            if (customPort != 0) {
//                port = customPort;
//            }
//            String ip = configs.getIp();
//            LogUtil.setPrefixFileName("api-http");
//            ControllerManager.initialize();
//            new HttpLogServer(ip, port).run();
//
//        } catch (Exception err) {
//            err.printStackTrace();
//        }
//    }
}
