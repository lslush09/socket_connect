package cg.smart.remote_cloud.thread;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import cg.smart.remote_cloud.CgSocketDelegate;
import cg.smart.remote_cloud.CgUploadClient;
import cg.smart.remote_cloud.helper.CgPackage;

/**
 * author: shun
 * created on: 2017/9/14 17:29
 * description:
 */
public class CgSendRunnable extends CgAbstractRunnable implements CgUploadClient.CgWriteListener {
    private LinkedBlockingQueue<CgPackage> sendingPacketQueue = new LinkedBlockingQueue();
    private OutputStream out;
    private CgSocketDelegate.ThreadSendDelegate delegate;
    private int minBufSize;
    public CgSendRunnable(int minBufSize,OutputStream out, CgSocketDelegate.ThreadSendDelegate delegate,long signal) {
        super(signal);
        this.out = out;
        this.delegate = delegate;
        this.minBufSize = minBufSize;
    }

    public void setSendPack(CgPackage cp){
        if(cp == null) return;
        try {
            sendingPacketQueue.put(cp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean write(byte[] data) throws IOException {
        synchronized (getLock()) {
            if(isRunning() && data != null){
                int length = Math.min(minBufSize,data.length);
                int offset = 0;
                while (offset < data.length) {
                    int end = offset + length;
                    end = Math.min(end, data.length);
                    out.write(data, offset, end - offset);
                    out.flush();
                    offset = end;
                }
                return true;
            }
            return false;
        }
    }


    @Override
    public void run() {
        CgPackage packet;
        try {
            while (isRunning() && (packet = sendingPacketQueue.take()) != null){
                byte[] data =  delegate.getConfig().getEn_delegate().encryptData(packet,delegate.getConfig().getCharsetName());
                write(data);
            }
            delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_n,"normal",signal);
        } catch (InterruptedException e) {
            e.printStackTrace();
            delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_exception,e.getMessage(),signal);
        } catch (IOException e) {
            e.printStackTrace();
            delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_io,e.getMessage(),signal);
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
