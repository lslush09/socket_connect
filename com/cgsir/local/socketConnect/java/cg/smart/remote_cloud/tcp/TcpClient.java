package cg.smart.remote_cloud.tcp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import cg.smart.remote_cloud.CgNewSockClient;
import cg.smart.remote_cloud.helper.CgPackage;
import cg.smart.remote_cloud.helper.CgSocketConfig;

/**
 * author: shun
 * created on: 2017/10/26 15:37
 * description:
 */
public class TcpClient {

    private final static Handler handle = new Handler(Looper.getMainLooper());

    public  static void send(final CgPackage cgPackage, final CgSocketConfig config, final TcpCallBack callBack){
        if(config == null){
            callBack.fail("config is null");
            return;
        }
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                Socket socket = null;
                OutputStream out = null;
                InputStream in = null;
                try {
                    socket = new Socket();
                    socket.setSoTimeout(config.getReadTimeOut());
                    socket.connect(config.getAddress().getInetSocketAddress(),config.getAddress().getConnectionTimeout());
                    if(socket.isConnected()){
                        out = socket.getOutputStream();
                        byte[] data =  config.getEn_delegate().encryptData(cgPackage,config.getCharsetName());
                        out.write(data);
                        Log.e(CgNewSockClient.TAG,"tcp send success");
                        in = socket.getInputStream();
                        byte[] header = readLength(config.getRule().headLength(),in);
                        if(header != null){
                            byte[] b = readLength(config.getRule().dataLength(header),in);
                            CgPackage pa = config.getEn_delegate().decryptData(header,b,config.getCharsetName());
//                            byte[] he = config.getEn_delegate().decryptHeadData(header,b);
                            RespondData(callBack,new String(pa.getBodyByte("")));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    result = e.getMessage();
                } finally {
                    if(socket != null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(out != null){
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(in != null){
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                RespondOver(callBack,s);
                Log.e(CgNewSockClient.TAG,"tcp = over ");
            }
        }.execute();

    }

    private static void RespondData(final TcpCallBack callBacks , final String success){
        if(callBacks == null) return;
        if(Looper.myLooper() != Looper.getMainLooper()){
            handle.post(new Runnable() {
                @Override
                public void run() {
                    RespondData(callBacks,success);
                }
            });
        }else{
            callBacks.success(success);
        }
    }


    private static void RespondOver(final TcpCallBack callBacks,final String error){
        if(callBacks == null) return;
        if(Looper.myLooper() != Looper.getMainLooper()){
            handle.post(new Runnable() {
                @Override
                public void run() {
                    RespondOver(callBacks,error);
                }
            });
        }else{
            callBacks.fail(error);
        }
    }

    public static byte[] readLength(int length,InputStream in) throws IOException{
        if (length <= 0) {
            return null;
        }
        if(in == null){
            throw new IOException("InputStream is closed");
        }
        byte[] buffer = new byte[length];
        int index = 0;
        int readCount;
        do {
            readCount = in.read(buffer, index, length - index);
            index += readCount;
        } while (readCount != -1 && index < length);

        if (index != length) {
            return null;
        }

        return buffer;
    }


    public interface TcpCallBack{
        void success(String result);
        void fail(String fail);
    }
}
