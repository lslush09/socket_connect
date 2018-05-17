package cg.smart.remote_cloud;

import android.content.Context;
import java.net.Socket;
import cg.smart.remote_cloud.helper.CgConnectDelegate;
import cg.smart.remote_cloud.helper.CgDataEDDelegate;
import cg.smart.remote_cloud.helper.CgMapQueue;
import cg.smart.remote_cloud.helper.CgPackage;
import cg.smart.remote_cloud.helper.CgSocketConfig;
import cg.smart.remote_cloud.helper.CgUpLoadDelegate;

/**
 * author: shun
 * created on: 2017/9/14 16:07
 * description:
 */
public interface CgSocketDelegate {
    void init(Context context,CgSocketConfig config);
    void connect();
    void disconnect();
    void registerConnectDelegate(CgConnectDelegate cDelegate);
    void unregisterConnectDelegate(CgConnectDelegate cDelegate);
    void sendPackage(CgPackage cp);
    void setAutoConnect(boolean autoConnect);
    CgSocketConfig getCfig();
    void upload(long task,String path);
    void cancel(long task);
    void registerUploadDelegate(CgUpLoadDelegate uDelegate);
    void unregisterUploadDelegate(CgUpLoadDelegate uDelegate);

    interface ThreadConnectDelegate extends ThreadDelegate{
        void connectOver(Socket socket,long signal);
    }
    interface ThreadDelegate{
        int e_code_n = 1;//正常退出
        int e_code_n_ing = 2;//运行中被关闭
        int e_code_io = 3;//发送IO 异常
        int e_code_exception = 4;//发送其他异常
        CgSocketConfig getConfig();
        void sendException(int code,String message,long signal);
    }

    interface ThreadSendDelegate extends ThreadDelegate{

    }

    interface ThreadReceiverDelegate extends ThreadDelegate{
        void respondData(byte[] head,byte[] body,long signal);
    }


    interface ThreadUploadDelegate extends ThreadDelegate{
        boolean write(byte[] bytes);
        void over(int result,CgMapQueue.CgEntry<Long,String> entry);
    }


}
