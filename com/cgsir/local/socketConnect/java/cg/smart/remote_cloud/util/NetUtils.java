package cg.smart.remote_cloud.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * author: shun
 * created on: 2017/9/13 17:38
 * description:
 */
public class NetUtils {
    private static NetworkInfo getActiveNetWorkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         return cm.getActiveNetworkInfo();
    }
    public static boolean haveNet(Context context){
        NetworkInfo info = getActiveNetWorkInfo(context);
        return info != null && info.isConnected();
    }
    public static boolean connectIsWifi(Context context){
        NetworkInfo info = getActiveNetWorkInfo(context);
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI ;
    }
}
