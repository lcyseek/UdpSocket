package com.example.tcpsocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private InputStream is;
    private OutputStream os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connect(View view) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
//                        socket = new Socket("192.168.89.27", 20003);
                        socket = new Socket();
                    }

                    //等待建立连接的时间.默认不超时
                    socket.connect(new InetSocketAddress(InetAddress.getByName("192.168.89.27"),20003),5000);
                    System.out.println("连接是否阻塞？阻塞！！！！");

                    is = socket.getInputStream();
                    os = socket.getOutputStream();

                    if(is == null || os == null)
                        System.out.println("connect failed!");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void send(View view) {
        if(os != null)
            try {
                os.write("hello tcp".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void recv(View view) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    byte[] buffer = new byte[500];
                    try {

                        if(socket.isClosed())
                            break;

                        int len = is.read(buffer,0,buffer.length);
                        if(len == -1)
                            break;

                        String text = new String(buffer,0,len);

                        if(text.equals("quit")) {
                            break;
                        }
                        else
                            System.out.println("recv:"+text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("退出接收");
            }
        });
    }

    private ServerSocket serverSocket;
    public void startServer(View view) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(serverSocket == null) {
                        serverSocket = new ServerSocket();
                        serverSocket.setReuseAddress(true);
                        serverSocket.bind(new InetSocketAddress(20004));
                    }

                    byte[] buffer = new byte[1024];
                    while (true){
                        Socket clientSocket = serverSocket.accept();
//                        System.out.println(socket.getInetAddress().getHostAddress());

                        InetSocketAddress socketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                        System.out.println("client ip = "+socketAddress.getAddress().getHostAddress());

                        InputStream is = clientSocket.getInputStream();
                        OutputStream os = clientSocket.getOutputStream();

                        int len;
                        while ((len = is.read(buffer))!= -1){
                            String text = new String(buffer,0,len);
                            System.out.println("recv:"+text);
                            os.write((text+" ack").getBytes());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


    }
}
