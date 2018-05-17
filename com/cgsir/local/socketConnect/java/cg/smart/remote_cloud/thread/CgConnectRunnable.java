package cg.smart.remote_cloud.thread;

import java.io.IOException;
import java.net.Socket;

import cg.smart.remote_cloud.CgAbstractSocketClient;
import cg.smart.remote_cloud.CgSocketDelegate;

/**
 * author: shun
 * created on: 2017/9/14 16:41
 * description:
 */
public class CgConnectRunnable extends CgAbstractRunnable {
    private CgSocketDelegate.ThreadConnectDelegate delegate;
    private Socket socket;
    public CgConnectRunnable(CgSocketDelegate.ThreadConnectDelegate delegate,long signal) {
        super(signal);
        this.delegate = delegate;
    }
    @Override
    public void close() {
        super.close();
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
            if(isRunning()){
                try {
                    socket = new Socket();
                    socket.connect(delegate.getConfig().getAddress().getInetSocketAddress(),delegate.getConfig().getAddress().getConnectionTimeout());
                    delegate.connectOver(socket,signal);
                } catch (IOException e) {
                    e.printStackTrace();
                    delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_io,e.getMessage(),signal);
                    if(socket != null){
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }else{
                delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_n_ing,"normal",signal);
            }
    }
}
