package com.koal.code2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
public class EchoApplication2 {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) throws IOException, InterruptedException {

        final int listenPort = 12345;

        // 启动服务端
        EchoServer2 server = new EchoServer2(listenPort);
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

class EchoServer2 {

    //固定输出
    private static final String PRINT_TIP = "Enter(h for help): ";
    private static final String FROM_SERVER = "(from SERVER)";
    private static final String LINK_FLAG = " - ";
    private static final String WELCOME = "Welcome to My Echo Server.";
    private static final String WELCOME_LINE = LINK_FLAG + WELCOME + FROM_SERVER;

    private int listenPort;

    private ServerSocket serverSocket;

    private static ThreadLocal<Socket> socketThreadLocal = new InheritableThreadLocal<>();

    public EchoServer2(int listenPort) {
        this.listenPort = listenPort;
    }

    public void startService() {

        try {
            serverSocket = new ServerSocket(this.listenPort);
            Socket socket1 = serverSocket.accept();


            Runnable runnable = () -> {
                try {
                    //获取服务端的socket
                    int port = socket1.getPort();
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket socket = serverSocket.accept();


                    //获取输入流
                    InputStream in = socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    //获取输出流
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.println(EchoApplication.getCurrentTime() + WELCOME_LINE);
                    out.println();
                    out.print(PRINT_TIP);
                    out.flush();


                    String input = null;
                    while ((input = br.readLine()) != null) {
                        //服务端原样输出
                        System.out.println(input);

                        //是否属于自定义命令
                        if ("h".equals(input)) {
                            out.println("The commands:");
                            out.println("----------------------------------------------------");
                            out.println("q\t\tquery current connections");
                            out.println("d id\t\tdisconnect client");
                            out.println("x\t\tquit server");
                            out.println("h\t\thelp");
                            out.println();
                            out.print(PRINT_TIP);
                            out.flush();
                            continue;
                        }

                        if ("x".equals(input)) {
                            out.println(EchoApplication.getCurrentTime() + LINK_FLAG + "The server has exited. Bye!");
                            socket.close();
                            break;
                        }


                        //Enter: What can i do for you?
                        //2020-03-31 16:59:12.828 - What can i do for you?(from SERVER)
                        //输出到客户端
                        out.printf(EchoApplication.getCurrentTime() + LINK_FLAG + "%s" + FROM_SERVER, input);
                        out.println();
                        out.println();
                        out.print(PRINT_TIP);
                        out.flush();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            Thread t1 = new Thread(runnable);
            t1.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}