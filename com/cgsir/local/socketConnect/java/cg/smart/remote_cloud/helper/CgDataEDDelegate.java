package cg.smart.remote_cloud.helper;

/**
 * author: shun
 * created on: 2017/9/13 10:15
 * description:
 */
public interface CgDataEDDelegate {
    byte[] encryptData(CgPackage cgPackage,String charset);
    CgPackage decryptData(byte[] head,byte[] body,String charset);
    byte[] sendUploadData(byte[] data,int index,int num,long task_id);
}
