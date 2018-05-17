package cg.smart.remote_cloud.thread;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import cg.smart.remote_cloud.CgSocketDelegate;
import cg.smart.remote_cloud.helper.CgMapQueue;

/**
 * author: shun
 * created on: 2017/11/9 18:17
 * description:
 */
public class CgUploadRunnable extends CgAbstractRunnable {
    public final static int SUCCESS = 1;
    public final static int FAIL = 2;
    public final static int CANCEL = 3;
    private final  static   int BLOCK = 1024*100; //分块上传,每块的大小是100k
    private CgMapQueue.CgEntry<Long,String> path;
    private long start_length = 0;
    private CgSocketDelegate.ThreadUploadDelegate delegate;
    public CgUploadRunnable(CgMapQueue.CgEntry<Long,String> path,CgSocketDelegate.ThreadUploadDelegate delegate) {
        super();
        this.path = path;
        this.delegate = delegate;
    }
    @Override
    public void run() {
        int io_code = CgSocketDelegate.ThreadDelegate.e_code_io;
        int result = SUCCESS;
        String message = "";
        if(delegate == null){
            result = FAIL;
            message = "delegate is null";
        }if(path != null && !TextUtils.isEmpty(path.getV())){
            File file = new File(path.getV());
            if(file.exists()){
                long length = file.length(); //获得数据的总长度
                if(start_length >= length){
                    start_length = 0;
                }
                length = length -start_length;
                int block_num = (int) (length%BLOCK == 0 ? length/BLOCK : length/BLOCK+1);
                byte[] cache = new byte[BLOCK];
                int len ;
                int num_count = 0;
                RandomAccessFile fis = null;
                try {
                    fis = new RandomAccessFile(file,"r");
                    fis.seek(start_length);
                    while (isRunning()) {
                        if((len = fis.read(cache, 0, cache.length)) == -1) break;
                        byte[] upload = delegate.getConfig().getEn_delegate().sendUploadData(Arrays.copyOf(cache,len),num_count,block_num,path.getK());
                       if(upload == null || !delegate.write(upload)){
                           message = "output error";
                           result = FAIL;
                           break;
                       }
                        num_count++;
                    }
                    if(num_count != block_num){
                        message = "cancel";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message = e.getMessage();
                    result = FAIL;
                }finally {
                    if(fis != null){
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                message = "file not exists";
                result = FAIL;
            }
        }else{
            message = "path is null";
            result = FAIL;
        }

        if(result == FAIL){
            delegate.sendException(io_code,message,signal);
        }
        delegate.over(result,path);
        setRunning(false);
    }
}
