package cg.smart.remote_cloud;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cg.smart.remote_cloud.helper.CgMapQueue;
import cg.smart.remote_cloud.helper.CgSocketConfig;
import cg.smart.remote_cloud.helper.CgUpLoadDelegate;
import cg.smart.remote_cloud.thread.CgUploadRunnable;

/**
 * author: shun
 * created on: 2017/11/10 09:08
 * description:
 */
public class CgUploadClient implements CgSocketDelegate.ThreadUploadDelegate {
    private static final int MAX_TASK = 5;
    private static CgUploadClient instance;
    private List<CgUpLoadDelegate> upLoadDelegates = new ArrayList<>();
    private Handler hander = new Handler();
    public static CgUploadClient getInstance(CgSocketConfig config){
        if(instance == null){
            instance = new CgUploadClient(config);
        }
        return instance;
    }
    private ExecutorService pool;
    private CgSocketConfig config;
    private CgWriteListener listener;

    void registerUploadDelegate(CgUpLoadDelegate uDelegate){
        upLoadDelegates.add(uDelegate);
    }
    void unregisterUploadDelegate(CgUpLoadDelegate uDelegate){
        upLoadDelegates.remove(uDelegate);
    }


    public void setListener(CgWriteListener listener) {
        this.listener = listener;
    }

    public CgUploadClient(CgSocketConfig config) {
        this.config = config;
    }

    private void initPool(){
        if(pool == null) {
            synchronized (this) {
                if(pool == null)
                pool = new ThreadPoolExecutor(1, MAX_TASK,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());
            }
        }
    }
    private CgMapQueue<Long,String> unexecuted = new CgMapQueue<>();
    private CgMapQueue<Long,CgUploadRunnable> executeing = new CgMapQueue<>();
    private CgMapQueue<Long,String> excuteed = new CgMapQueue<>();
    private CgMapQueue<Long,String> excuteed_fail = new CgMapQueue<>();


    public void putTask(long task_id,String file_path){
        putTask(task_id,file_path,true);
    }
    public synchronized void putTask(long task_id,String file_path,boolean replace){
        unexecuted.put(task_id,file_path,replace);
        startRun();
    }

    public synchronized void cancelTask(long task_id){
        if(unexecuted.containsKey(task_id)){
            unexecuted.remove(task_id);
        }else if(executeing.containsKey(task_id)){
            //已经在执行了... executeing
            CgUploadRunnable upload = executeing.getValue(task_id);
            if(upload != null){
                upload.close();
            }
        }
    }

    private synchronized void startRun(){
        if(config == null) throw new NullPointerException("config is null");
        if(listener == null) throw new NullPointerException("write listener is null,please call setListener");
        if(executeing.size() < MAX_TASK){
            initPool();
            CgMapQueue.CgEntry<Long,String> entry = unexecuted.poll();
            if(entry != null){
                CgUploadRunnable cg = new CgUploadRunnable(entry,this);
                pool.submit(cg);
                executeing.put(entry.getK(),cg);
            }
        }
    }

    @Override
    public boolean write(byte[] bytes) {
        try {
            return listener.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized void over(int result, CgMapQueue.CgEntry<Long, String> entry) {
        if(entry == null) return;
        if(executeing.containsKey(entry.getK())){
            executeing.remove(entry.getK());
        }
        if(result == CgUploadRunnable.SUCCESS){
            excuteed.put(entry.getK(),entry.getV());
        }else if (result == CgUploadRunnable.FAIL){
            excuteed_fail.put(entry.getK(),entry.getV());
        }
        startRun();
        notifyUploadDelegate(result,entry.getK());
    }

    @Override
    public CgSocketConfig getConfig() {
        return config;
    }

    @Override
    public void sendException(int code, String message, long signal) {
        Log.e(CgNewSockClient.TAG,message);
    }

    private void notifyUploadDelegate(final int result, final long task_id){
        if(Looper.myLooper() != hander.getLooper()){
            hander.post(new Runnable() {
                @Override
                public void run() {
                    notifyUploadDelegate(result, task_id);
                }
            });
        }else {
            for (CgUpLoadDelegate delegate : upLoadDelegates) {
                if (result == CgUploadRunnable.SUCCESS) {
                    delegate.uploadSuccess(task_id);
                } else if (result == CgUploadRunnable.FAIL) {
                    delegate.uploadFail(task_id);
                }else if (result == CgUploadRunnable.CANCEL) {
                    delegate.uploadCancel(task_id);
                }
            }
        }
    }

    public interface CgWriteListener{
        boolean write(byte[] data) throws IOException;
    }

}
