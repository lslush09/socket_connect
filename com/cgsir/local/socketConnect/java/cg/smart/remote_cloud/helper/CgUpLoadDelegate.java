package cg.smart.remote_cloud.helper;

/**
 * author: shun
 * created on: 2017/12/25 13:42
 * description:
 */
public interface CgUpLoadDelegate {
    void uploadSuccess(long task_id);
    void uploadCancel(long task_id);
    void uploadFail(long task_id);
}
