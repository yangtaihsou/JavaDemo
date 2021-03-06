package net.socket.longconnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 长连接迭代式的服务器(阻塞IO模型)
 * 主要为了模拟迭代长连接，没关注代码健壮
 * User: yangkuan@jd.com
 * Date: 18-6-28
 * Time: 下午12:42
 */
public class SocketLongServer {
    public static void main(String[] args) throws IOException {
        SocketLongServer socketService = new SocketLongServer();
        //1、a)创建一个服务器端Socket，即SocketService
        socketService.oneServer();
    }

    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 6, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));

    public void oneServer() {
        try {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(5209);
                //b)指定绑定的端口，并监听此端口。
                System.out.println("服务器启动成功");
                //创建一个ServerSocket在端口5209监听客户请求
            } catch (Exception e) {
                System.out.println("没有启动监听：" + e);
                //出错，打印出错信息
            }
            Socket socket = null;
            try {

                IOAsyThread ioAsyThread = new IOAsyThread();
                ioAsyThread.start();
                while (true) {
                    socket = serverSocket.accept();
                    //2、调用accept()方法开始监听，等待客户端的连接
                    //使用accept()阻塞等待客户请求，有客户
                    //请求到来则产生一个Socket对象，并继续执行
                    System.out.println("***accept*******");
                    //   ioAsyThread.run();
                    try {
                        ioAsyThread.addClientConnection(socket);
                       // executor.execute(ioAsyThread);
                    } catch (Exception e) {
                        try {
                            ioAsyThread.writeMsgToClient(socket.getOutputStream(), "服务端拒绝，请重试");
                            socket.close();
                        } catch (Exception e1) {
                            System.out.println("异步线程池塞满,回写客户端异常:" + e1.getMessage());
                        }
                        System.out.println("异步线程池塞满:" + e.getMessage());
                    }

                }
            } catch (Exception e) {
                System.out.println("Error." + e);
                //出错，打印出错信息
            }

        } catch (Exception e) {//出错，打印出错信息
            System.out.println("Error." + e);
        }
    }
}

class IOAsyThread extends Thread {

    private List<Socket> clientSocketList = new ArrayList<Socket>();
    public void  addClientConnection(Socket conSocket) {
        clientSocketList.add(conSocket);
    }


    public void run() {
        while (true) {
            try {

               // System.out.println("****received message from client******");
                if(clientSocketList.size()!=0){
                    for(Socket clientSocket:clientSocketList){
                    //读取客户端传过来的数据
                    readMessageFromClient(clientSocket);
                    }
                }else{
                Thread.sleep(4000);
                }
                // connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
         /*   if (connection!=null) {
                try {
            writer.close();
                    connection .close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            }
        }
    }

    /**
     * 读取客户端信息
     *
     * @param clientSocket
     */
    private void readMessageFromClient(Socket clientSocket) throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
            //  System.out.println("循环取client端数据");
            byte[] data = new byte[128];
            int len = inputStream.read(data);
            if (len > 0) {
                String clientSendInfo = new String(data, 0, len);
                    System.out.println("readMessageFromClient:" + clientSendInfo);
                    //向客户端写入数据
                    writeMsgToClient(clientSocket.getOutputStream(), "I am server message!!!"+clientSendInfo);
            }

    }

    /**
     * 响应客户端信息
     *
     * @param outputStream
     * @param string
     */
    public void writeMsgToClient(OutputStream outputStream, String string) throws IOException {
        System.out.println("writeMsgToClient:" + string);
        Writer writer = new OutputStreamWriter(outputStream);
        writer.append(string);
        writer.flush();
    }
}