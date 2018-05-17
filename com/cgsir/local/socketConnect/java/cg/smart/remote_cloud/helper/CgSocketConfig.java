package cg.smart.remote_cloud.helper;

import cg.smart.remote_cloud.util.ByteConvert;
import cg.smart.remote_cloud.util.CharsetUtil;

/**
 * author: shun
 * created on: 2017/9/12 18:05
 * description:
 */
public class CgSocketConfig {
    /**
     * 设置文件的编解码方式
     */
    private String charsetName = CharsetUtil.UTF_8;

    public String getCharsetName() {
        return charsetName;
    }

    public CgSocketConfig setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    private CgSocketAddress address;

    public CgSocketAddress getAddress() {
        if(address == null){
            address = new CgSocketAddress();
        }
        return address;
    }

    public CgSocketConfig setAddress(CgSocketAddress address) {
        this.address = address;
        return this;
    }

    private long hearTimePeriod = 20*1000;

    public long getHearTimePeriod() {
        return hearTimePeriod;
    }

    public void setHearTimePeriod(long hearTimePeriod) {
        this.hearTimePeriod = hearTimePeriod;
    }

    private CgPackage cgHeartPackage;

    public CgPackage getCgHeartPackage() {
        return cgHeartPackage;
    }

    public void setCgHeartPackage(CgPackage cgHeartPackage) {
        this.cgHeartPackage = cgHeartPackage;
    }

    private boolean receiverIsMainThread = false;

    public boolean isReceiverIsMainThread() {
        return receiverIsMainThread;
    }

    public void setReceiverIsMainThread(boolean receiverIsMainThread) {
        this.receiverIsMainThread = receiverIsMainThread;
    }

    private int readTimeOut = 5 * 1000;

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    private boolean receiverHeart = false;

    public boolean isReceiverHeart() {
        return receiverHeart;
    }

    public void setReceiverHeart(boolean receiverHeart) {
        this.receiverHeart = receiverHeart;
    }

    public interface IsHeartListener{
        boolean isHeart(byte[] head,byte[] body);
    }

    private static final CgDataEDDelegate default_data_delegate = new CgDataEDDelegate() {
        @Override
        public byte[] encryptData(CgPackage cgPackage,String charset) {
            return ByteConvert.merge(cgPackage.getHeadByte(),cgPackage.getBodyByte(charset));
        }

        @Override
        public CgPackage decryptData(byte[] head, byte[] body,String charset) {
            return new SimplePackage(head,body);
        }

        @Override
        public byte[] sendUploadData(byte[] data, int index, int num, long task_id) {
            return null;
        }
    };
    protected CgDataEDDelegate en_delegate;

    public CgDataEDDelegate getEn_delegate() {
        return en_delegate == null? default_data_delegate : en_delegate;
    }

    public void setEn_delegate(CgDataEDDelegate en_delegate) {
        this.en_delegate = en_delegate;
    }

    private static final CgPackage.ParsePackageDelegate default_rule =  new CgPackage.ParsePackageDelegate() {
        @Override
        public int headLength() {
            return 16;
        }

        @Override
        public int dataLength(byte[] head) {
            int length = head[3] == 0x1? 16 : 0;
            return length + ByteConvert.readInt32(head,8);
        }
    };

    private CgPackage.ParsePackageDelegate rule;

    public CgPackage.ParsePackageDelegate getRule() {
        return rule == null ? default_rule : rule;
    }

    public void setRule(CgPackage.ParsePackageDelegate rule) {
        this.rule = rule;
    }

    private static final CgSocketConfig.IsHeartListener default_heartListener = new CgSocketConfig.IsHeartListener(){
        @Override
        public boolean isHeart(byte[] head, byte[] body) {
            return ByteConvert.readInt16(head,4) == 0x2014;
        }
    };

    private CgSocketConfig.IsHeartListener heartListener;

    public IsHeartListener getHeartListener() {
        return heartListener == null ? default_heartListener : heartListener;
    }

    public void setHeartListener(IsHeartListener heartListener) {
        this.heartListener = heartListener;
    }

}
