package cg.smart.remote_cloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cg.smart.remote_cloud.helper.CgConnectDelegate;
import cg.smart.remote_cloud.helper.CgPackage;
import cg.smart.remote_cloud.helper.CgSocketConfig;
import cg.smart.remote_cloud.helper.CgUpLoadDelegate;
import cg.smart.remote_cloud.thread.CgConnectRunnable;
import cg.smart.remote_cloud.thread.CgReceiverRunnable;
import cg.smart.remote_cloud.thread.CgSendRunnable;
import cg.smart.remote_cloud.util.NetUtils;

/**
 * author: shun
 * created on: 2017/9/14 16:11
 * description:
 */
public abstract class CgAbstractSocketClient implements CgSocketDelegate,CgSocketDelegate.ThreadConnectDelegate,
        CgSocketDelegate.ThreadSendDelegate,CgSocketDelegate.ThreadReceiverDelegate, CgUploadClient.CgWriteListener {
    protected C_STATE state = C_STATE.DISCONNECTED;
    private boolean isInit = false;
    private Context mContext;
    private ArrayList<CgConnectDelegate> connectDelegates = new ArrayList<>();
    private ExecutorService pool;
    protected CgConnectRunnable c_runnable;
    protected CgSendRunnable s_runnable;
    protected CgReceiverRunnable r_runnable;
    private boolean autoConnect = true;
    private Handler handler;
    private CgUploadClient uploadClient;
    @Override
    public void init(Context context,CgSocketConfig config) {
        if(config == null || config.getAddress() == null) throw new RuntimeException("please set Ip and port");
        mContext = context;
        this.config = config;
        handler = new Handler();
        pool = new ThreadPoolExecutor(3, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        isInit = true;
        registerReceiver();
        executeHear();
        uploadClient = CgUploadClient.getInstance(config);
        uploadClient.setListener(this);
    }

    @Override
    public void registerUploadDelegate(CgUpLoadDelegate uDelegate) {
        uploadClient.registerUploadDelegate(uDelegate);
    }

    @Override
    public void unregisterUploadDelegate(CgUpLoadDelegate uDelegate) {
        uploadClient.unregisterUploadDelegate(uDelegate);
    }

    @Override
    public boolean write(byte[] data) throws IOException{
        return s_runnable != null && s_runnable.write(data);
    }

    @Override
    public void upload(long task, String path) {
        if(uploadClient != null){
            uploadClient.putTask(task, path);
        }
    }

    @Override
    public void cancel(long task) {
        if(uploadClient != null){
            uploadClient.cancelTask(task);
        }
    }

    private long hear_time ;

    public void setHear_time(long hear_time) {
        this.hear_time = hear_time;
    }

    public long getHear_time() {
        return hear_time;
    }

    private boolean heartExecute(){
        return (System.currentTimeMillis() - getHear_time()) < (config.getHearTimePeriod()+ 5*1000);
    }


    private Timer hearBeat = null;
    protected void executeHear() {
        if (this.hearBeat == null) {
            hearBeat = new Timer("cg_heart_thread");
            hearBeat.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(getConfig().getCgHeartPackage() == null) return;
                    if(heartExecute()) {
                        sendPackage(getConfig().getCgHeartPackage());
                    }else if (autoConnect){
                        errorDisConnect();
                        connect();
                    }
                }
            },5*1000,getConfig().getHearTimePeriod());
        }
    }

    protected void notifyNoNetWork(){
        if(Looper.myLooper() != handler.getLooper()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyNoNetWork();
                }
            });
        }else{
            for (CgConnectDelegate cd:connectDelegates) {
                cd.onNoNetConnect();
            }
        }
    }

    protected void notifyConnect(){
        if(Looper.myLooper() != handler.getLooper()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyConnect();
                }
            });
        }else{
            for (CgConnectDelegate cd:connectDelegates) {
                cd.onConnected();
            }
        }
    }

    protected void notifyDisConnect(final String message,final boolean isSelf){
        if(Looper.myLooper() != handler.getLooper()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDisConnect(message,isSelf);
                }
            });
        }else{
            for (CgConnectDelegate cd:connectDelegates) {
                cd.onDisconnected(message,isSelf);
            }
        }
    }



    protected void notifyData(byte[] head,byte[] body){
        if(getConfig().isReceiverIsMainThread()){
            notifyDataMain(head,body);
        }else{
            notifyDataNoMain(head,body);
        }

    }
    private void notifyDataMain(final byte[] head,final byte[] body){
        if(Looper.myLooper() != handler.getLooper()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataMain(head,body);
                }
            });
        }else{
            notifyDataNoMain(head,body);
        }
    }
    private void notifyDataNoMain(byte[] head,byte[] body){
        for (CgConnectDelegate cd:connectDelegates) {
            cd.onRespond(head,body,Looper.myLooper() == handler.getLooper());
        }
    }


    protected boolean isDisconnected(){
        return state == C_STATE.DISCONNECTED;
    }
    protected boolean isConnected(){
        return state == C_STATE.CONNECTED;
    }
    protected boolean isConnecting(){
        return state == C_STATE.CONNECTING;
    }
    protected CgSocketConfig config;

    @Override
    public CgSocketConfig getCfig() {
        if(config == null){
            config = new CgSocketConfig();
        }
        return config;
    }

    @Override
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    @Override
    public void connect() {
        if(!isInit) throw new RuntimeException("please call init");
        if(!NetUtils.haveNet(mContext)){
            notifyNoNetWork();
            return;
        }
        synchronized (this) {
            if (!isDisconnected()) return;
            if (c_runnable != null) new RuntimeException("why connect runnable is not null");
            c_runnable = new CgConnectRunnable(this,setSignal());
            pool.submit(c_runnable);
            setState(C_STATE.CONNECTING);
        }
    }

    protected void submitRunnable(Runnable runnable){
         pool.submit(runnable);
    }


    protected boolean errorDisConnect(){
        synchronized (this){
            if(isDisconnected()) return false;
            setState(C_STATE.DISCONNECTING);
            //关闭发送线程
            if(s_runnable != null){
                s_runnable.close();
                s_runnable.setSendPack(CgPackage.empty);
                s_runnable = null;
            }
            //关闭接受线程
            if(r_runnable != null){
                r_runnable.close();
                r_runnable = null;
            }
            //关闭连接线程
            if(c_runnable != null){
                c_runnable.close();
                c_runnable = null;
            }
            setState(C_STATE.DISCONNECTED);
        }
        return true;
    }

    @Override
    public void disconnect() {
        if(errorDisConnect()){
            notifyDisConnect("normal",true);
        }
    }

    @Override
    public synchronized void sendPackage(CgPackage cp) {
        if(isConnected()){
            s_runnable.setSendPack(cp);
        }
    }

    @Override
    public void registerConnectDelegate(CgConnectDelegate cDelegate) {
        if(cDelegate != null){
            connectDelegates.add(cDelegate);
        }
    }

    @Override
    public void unregisterConnectDelegate(CgConnectDelegate cDelegate) {
        connectDelegates.remove(cDelegate);
    }



    public enum C_STATE{
        CONNECTING, CONNECTED,DISCONNECTING,DISCONNECTED
    }



    protected void setState(C_STATE state){
        this.state = state;
    }

    private long signal = 0;
    protected long setSignal(){
            return signal = System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    protected long getSignal(){
        return signal;
    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION || intent.getAction() == NEW_CHANGE){
                if(NetUtils.haveNet(context)){
                    if(isDisconnected()) {
                        errorDisConnect();
                        if (autoConnect) {
                            connect();

                        }
                    }
                }else{
                    errorDisConnect();
                    notifyNoNetWork();
                }
            }
        }
    };
    public final static String NEW_CHANGE = "cg.smart.net_change";
    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(NEW_CHANGE);
        mContext.registerReceiver(broadcastReceiver,intentFilter);
    }
    private void unRegisterReceiver(){
        mContext.unregisterReceiver(broadcastReceiver);
    }

}
