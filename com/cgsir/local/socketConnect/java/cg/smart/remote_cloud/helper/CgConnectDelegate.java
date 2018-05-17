package cg.smart.remote_cloud.helper;

/**
 * author: shun
 * created on: 2017/9/12 18:52
 * description:
 */
public interface CgConnectDelegate {
    void onConnected();
    void onDisconnected(String message,boolean isSelf);
    void onNoNetConnect();
    void onRespond(byte[] head,byte[] body,boolean isMainThread);
}
