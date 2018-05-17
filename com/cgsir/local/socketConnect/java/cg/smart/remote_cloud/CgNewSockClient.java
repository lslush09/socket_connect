package cg.smart.remote_cloud;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import cg.smart.remote_cloud.helper.CgPackage;
import cg.smart.remote_cloud.helper.CgSocketConfig;
import cg.smart.remote_cloud.thread.CgReceiverRunnable;
import cg.smart.remote_cloud.thread.CgSendRunnable;


/**
 * author: shun
 * created on: 2017/9/14 17:02
 * description:
 */
public class CgNewSockClient extends CgAbstractSocketClient {
    public static final String TAG = "cg_connect";
    private static CgNewSockClient instance;
    public static final CgNewSockClient getInstance(){
        if(instance == null){
            instance = new CgNewSockClient();
        }
        return instance;
    }

    @Override
    public synchronized void connectOver(Socket socket,long signal) { //在Connect中被锁着
        if(!isConnecting()) {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int minBufSize ;
        OutputStream out ;
        InputStream in ;
        try {
            minBufSize = socket.getSendBufferSize();
            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            sendException(ThreadDelegate.e_code_io,e.getMessage(),signal);
            return;
        }
        s_runnable = new CgSendRunnable(minBufSize,out,this,getSignal());
        r_runnable = new CgReceiverRunnable(in,this,getSignal());
        submitRunnable(s_runnable);
        submitRunnable(r_runnable);
        setState(CgAbstractSocketClient.C_STATE.CONNECTED);
        setHear_time(System.currentTimeMillis());
        notifyConnect();
    }

    /*@Override
    public synchronized void connectFailed(int code,String message,long signal) {//在Connect中被锁着
        if(!isConnecting()) return;
        if(signal != getSignal()) return;
        boolean result = errorDisConnect();
        switch (code){
            case ThreadDelegate.e_code_io:
                if(result) {
                    notifyDisConnect(message, false);
                }
                break;
            case ThreadDelegate.e_code_n_ing:
                //自然断开
                break;
        }
    }*/


    @Override
    public CgSocketConfig getConfig() {
        return config;
    }


    @Override
    public synchronized void sendException(int code,String message,long signal) {
        if(signal != getSignal()) return;
       boolean result =  errorDisConnect();
        switch (code){
            case ThreadDelegate.e_code_io: //接受线程非正常退出,可能是接受线程,发送线程
                if(result) {
                    notifyDisConnect(message, false);
                }
                break;
            case ThreadDelegate.e_code_exception: //接受线程非正常退出,可能是接受线程,发送线程
                if(result) {
                    notifyDisConnect(message, false);
                }
                break;
        }
    }

    @Override
    public void respondData(byte[] head, byte[] body,long signal) {
        if(signal != getSignal()) return;
        if(!isConnected()) return;
        Log.e(TAG,"收到数据包");
        if(getConfig().getHeartListener().isHeart(head, body)) {
            Log.e(TAG,"心跳包..");
            setHear_time(System.currentTimeMillis());
            if(!config.isReceiverHeart()) return;
        }
        enCodeData(head, body);
    }

    public void enCodeData(byte[] head, byte[] body){
        CgPackage cgPackage = getConfig().getEn_delegate().decryptData(head, body,config.getCharsetName());
        if(cgPackage != null) {
            notifyData(cgPackage.getHeadByte(), cgPackage.getBodyByte(""));
        } else Log.e(TAG,"空数据..");
    }


}
