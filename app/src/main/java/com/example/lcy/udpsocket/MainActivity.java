package com.example.lcy.udpsocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private EditText et_ip;
    private EditText et_text;

    private DatagramSocket socket;
    private DatagramPacket packet;

    private ExecutorService pool;
    private RecvRunnable recvRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_ip = (EditText) findViewById(R.id.et_ip);
        et_text = (EditText) findViewById(R.id.et_text);
        et_ip.setText("192.168.89.27");

        pool = Executors.newFixedThreadPool(5);
    }

    public void startRecv(View view) {
        recvRunnable = new RecvRunnable();
        pool.execute(recvRunnable);
    }

    class RecvRunnable implements Runnable {
        private DatagramSocket socket;
        private DatagramPacket packet;
        private boolean quit = false;

        public void stop() {
            quit = true;
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                if (socket == null) {
                    //#1
                    //socket = new DatagramSocket(20002);//创建实例 并固定监听port端口的报文

                    //#2
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(20002));

                    //#3
                    //socket = new DatagramSocket(20002，InetAddress localAddr); 当一台机器拥有多余一个ip地址的时候，由它创建仅仅接收来自localAddr的报文

                    //设置超时时间
                    socket.setSoTimeout(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);

            while (!quit) {
                try {
                    socket.receive(packet);
                    String recv = new String(packet.getData(), 0, packet.getLength());

                    InetAddress inetAddress = packet.getAddress();//192.168.89.27
                    InetSocketAddress socketAddress = (InetSocketAddress) packet.getSocketAddress();//192.168.89.27:50701

                    System.out.println("inetAddress=" + inetAddress);
                    System.out.println("socketAddress=" + socketAddress);


                    Log.i(TAG, "run: 接收数据 ip=" + packet.getAddress().getHostAddress() + " port=" + packet.getPort() + " data=" + recv);

//                        也可以
//                        String temp = new String(buffer,0,packet.getLength());
//                        System.out.println("temp--->"+temp);
                } catch (Exception e) {
                    if(e instanceof SocketTimeoutException)
                        System.out.println("超时接收了!!!!!");
                    else
                        e.printStackTrace();
                }

            }
        }
    }

    public void send(View view) {
        final String ip = et_ip.getText().toString();
        final String text = et_text.getText().toString();
        final byte[] buffer = text.getBytes();

        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    if (socket == null)
                        socket = new DatagramSocket();

                    //数据报的载体
                    if (packet == null)
                        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), 20002);
                    else
                        packet.setData(buffer);

                    socket.send(packet);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socket != null)
            socket.close();

        if (recvRunnable != null)
            recvRunnable.stop();
    }

    public void inetAddress(View view) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //InetAddress 是Java对IP地址的封装。
                    //传入主机名字，InetAddress会尝试连接DNS的服务器，并且获取ip地址
                    InetAddress address = InetAddress.getByName("www.baidu.com");
                    System.out.println(address.getHostName()+"="+address.getHostAddress());

                    //根据主机名返回其有可能的InetAddress
                    InetAddress[] addresses = InetAddress.getAllByName("www.baidu.com");
                    for(InetAddress a : addresses){
                        System.out.println(address.getHostName()+"="+address.getHostAddress());
                    }

                    //localhost=127.0.0.1
                    System.out.println(InetAddress.getLocalHost().getHostName()+"="+InetAddress.getLocalHost().getHostAddress());

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void inetSocketAddress(View view) {
        //InetSocketAddress 是SocketAddress的实现子类
        //此类实现ip套接字地址(ip+端口号) 不依赖任何协议 表面看比InetAddress多了一个端口号
    }
}
