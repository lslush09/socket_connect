package cg.smart.remote_cloud.thread;

import java.io.IOException;
import java.io.InputStream;

import cg.smart.remote_cloud.CgSocketDelegate;
import cg.smart.remote_cloud.helper.CgInputReader;

/**
 * author: shun
 * created on: 2017/9/14 18:42
 * description:
 */
public class CgReceiverRunnable extends CgAbstractRunnable {
    private CgInputReader in;
    private CgSocketDelegate.ThreadReceiverDelegate delegate;
    public CgReceiverRunnable(InputStream in, CgSocketDelegate.ThreadReceiverDelegate delegate,long signal) {
        super(signal);
        this.in = new CgInputReader(in);
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try {
            while (isRunning()){
                byte[] header = in.readLength(delegate.getConfig().getRule().headLength(),false);
                if(header == null) continue;
                byte[] b = in.readLength(delegate.getConfig().getRule().dataLength(header),false);

//                byte[] body = delegate.getEDDelegate().decryptData(header,b);
//                byte[] he = delegate.getEDDelegate().decryptHeadData(header,b);
                delegate.respondData(header,b,signal);
            }
            delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_n,"normal",signal);
        } catch (IOException e) {
            e.printStackTrace();
            delegate.sendException(CgSocketDelegate.ThreadDelegate.e_code_io,e.getMessage(),signal);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
