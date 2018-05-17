package cg.smart.remote_cloud.helper;

/**
 * author: shun
 * created on: 2017/11/10 15:20
 * description:
 */
public class SimplePackage implements CgPackage {
    private byte[] head;
    private byte[] body;

    public SimplePackage(byte[] head, byte[] body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public byte[] getHeadByte() {
        return head;
    }

    @Override
    public byte[] getBodyByte(String charName) {
        return body;
    }
}
