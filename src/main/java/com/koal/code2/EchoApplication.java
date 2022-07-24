package com.koal.code2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

/**
 * 实现要求：
 * 1、根据代码片段实现一个简单的SOCKET ECHO程序；
 * 2、接受到客户端连接后，服务端返回一个欢迎消息;
 * 3、接受到"Good Bye!"消息后， 服务端返回一个结束消息，并结束当前会话;
 * 4、采用Windows自带的Telnet作为客户端，通过Telnet连接本服务端；
 * 5、服务端支持接受多个Telnet客户端连接;
 * 6、服务端支持简单命令操作：1）支持查看当前连接数 2）可断开指定客户端连接；
 * 7、注意代码格式与注释
 */
public class EchoApplication {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) throws IOException, InterruptedException {

        final int listenPort = 12345;

        // 启动服务端
        EchoServer server = new EchoServer(listenPort);
        server.startService();

        // 服务端启动后，运行结果示例：
        /**
         java -cp ./classes EchoApplication

         2020-03-31 16:58:44.049 - Welcome to My Echo Server.(from SERVER)
         The current connections:
         Id.			Client				LogonTime
         -----------------------------------------------------
         1			127.0.0.1:32328		2020-03-31 16:59:13
         2			127.0.0.1:43434		2020-03-31 17:03:02
         3			127.0.0.1:39823		2020-03-31 07:03:48

         Enter(h for help): h
         The commands:
         ----------------------------------------------------
         q		query current connections
         d id		disconnect client
         x		quit server
         h		help

         Enter(h for help): d 1
         2020-03-31 16:58:44.049 - The connection '127.0.0.1:32328' has been disconnected.
         The current connections:
         Id.			Client				LogonTime
         -----------------------------------------------------
         1			127.0.0.1:43434		2020-03-31 17:03:02
         2			127.0.0.1:39823		2020-03-31 07:03:48

         Enter(h for help): x
         2020-03-31 16:58:44.049 - The server has exited. Bye!
         */

        // 在telnet控制台输入，服务端直接原文返回输入信息
        // 客户端结果示例：
        /**
         2020-03-31 16:58:44.049 - Welcome to My Echo Server.(from SERVER)

         Enter: hello!
         2020-03-31 16:58:55.452 - hello!(from SERVER)

         Enter: This is KOAL.
         2020-03-31 16:59:06.565 - This is KOAL.(from SERVER)

         Enter: What can i do for you?
         2020-03-31 16:59:12.828 - What can i do for you?(from SERVER)

         Enter: Good Bye!
         2020-03-31 16:59:16.502 - Bye bye!(from SERVER)
         */


        // 此处填写上所编写程序输出结果：
        /**
         */

    }

    public static String getCurrentTime() {
        //2020-03-31 16:58:44.049
        LocalDateTime now = LocalDateTime.now();
        return DATE_TIME_FORMATTER.format(now);
    }

}

/**
 * 需要支持多个客户端，使用NIO异步非阻塞的模型
 */
class EchoServer {

    //固定输出
    private static final String PRINT_TIP = "Enter(h for help): ";
    private static final String FROM_SERVER = "(from SERVER)";
    private static final String LINK_FLAG = " - ";
    private static final String WELCOME = "Welcome to My Echo Server.";
    private static final String WELCOME_LINE = LINK_FLAG + WELCOME + FROM_SERVER;

    private int listenPort;

    public EchoServer(int listenPort) {
        this.listenPort = listenPort;
    }

    public void startService() {

        try {
            //创建通道，设置非阻塞模式，监听端口并注册
            ServerSocketChannel sChannel = ServerSocketChannel.open();
            sChannel.configureBlocking(false);
            sChannel.bind(new InetSocketAddress(this.listenPort));
            Selector selector = Selector.open();
            sChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey sk = it.next();
                    //首次连接服务端
                    if (sk.isAcceptable()) {
                        //若"接受就绪",获取客户端连接，设置阻塞模式，将该通道注册到服务器上
                        SocketChannel socketChannel = sChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        //输出欢迎语句
                        String welcome = EchoApplication.getCurrentTime() + WELCOME_LINE;
                        socketChannel.write(ByteBuffer.wrap(welcome.getBytes(StandardCharsets.UTF_8)));
                        //输出当前已连接的客户端信息 TODO

                    } else if (sk.isReadable()) {
                        // 获取当前选择器"就绪" 状态的通道
                        SocketChannel socketChannel = (SocketChannel) sk.channel();

                        // 读取数据
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        int len = 0;
                        while ((len = socketChannel.read(buf)) > 0) {
                            buf.flip();
                            String input = new String(buf.array(), 0, len);
                            System.out.print(input);


                            //是否属于自定义命令
                            if ("h".equals(input)) {

                                continue;
                            }

                            if ("x".equals(input)) {
                                socketChannel.close();
                                break;
                            }

                            buf.clear();
                        }


                    }
                    it.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}