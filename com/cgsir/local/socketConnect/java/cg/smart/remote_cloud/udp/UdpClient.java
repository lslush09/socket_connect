package cg.smart.remote_cloud.udp;


import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import cg.smart.remote_cloud.CgNewSockClient;

/**
 * author: shun
 * created on: 2017/10/25 10:51
 * description:
 */
public class UdpClient {
    private final static Handler handle = new Handler(Looper.getMainLooper());

    public  static void send(String message,String address,int port,final UdpCallBack callBack){
        new AsyncTask<String,Void,String>(){
            @Override
            protected String doInBackground(String... params) {
                DatagramSocket client = null;
                String result = "";
                try {
                    byte[] sendBuf = params[0].getBytes();
                    client = new DatagramSocket();
                    client.setSoTimeout(8 * 1000);
                    InetAddress inetAddress = InetAddress.getByName(params[1]);
                    DatagramPacket sendPacket
                            = new DatagramPacket(sendBuf ,sendBuf.length , inetAddress , Integer.parseInt(params[2]));
                    client.send(sendPacket);
                    byte[] receiverBuf = new byte[1024];
                    while (true) {
                        DatagramPacket receiverPacket
                                = new DatagramPacket(receiverBuf, receiverBuf.length);
                        client.receive(receiverPacket);
                        String receiverStr = new String(receiverPacket.getData(), 0, receiverPacket.getLength());
                        RespondData(callBack,receiverStr,receiverPacket.getAddress().getHostAddress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = e.getMessage();
                } finally {
                    if(client != null){
                        client.close();
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                RespondOver(callBack,s);
                Log.e(CgNewSockClient.TAG,"udp = over ");
            }
        }.execute(message,address,String.valueOf(port));

    }

    private static void RespondData(final UdpCallBack callBacks ,final String success,final String ip){
        if(callBacks == null) return;
        if(Looper.myLooper() != Looper.getMainLooper()){
            handle.post(new Runnable() {
                @Override
                public void run() {
                    RespondData(callBacks,success,ip);
                }
            });
        }else{
            callBacks.success(success,ip);
        }
    }


    private static void RespondOver(final UdpCallBack callBacks,final String s){
        if(callBacks == null) return;
        if(Looper.myLooper() != Looper.getMainLooper()){
            handle.post(new Runnable() {
                @Override
                public void run() {
                    RespondOver(callBacks,s);
                }
            });
        }else{
            callBacks.fail(s);
        }
    }

    public interface UdpCallBack{
        void success(String result,String ip);
        void fail(String error);
    }


}
