package cg.smart.remote_cloud.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * author: shun
 * created on: 2017/9/13 10:36
 * description:
 */
public class ByteConvert {
    /**
     * short转byte数组 低位在前,高位在后
     */
    public static byte[] ShortToBytes(short n)
    {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }
    /**
     * 整形转byte数组 低位在前,高位在后
     */
    public static byte[] IntToBytes(int n)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) ((n >> 8) & 0xff);
        b[2] = (byte) ((n >> 16) & 0xff);
        b[3] = (byte) (n >> 24);
        return b;
    }

    public static byte[] LongToBytes(long n)
    {
        byte[] b = new byte[8];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) ((n >> 8) & 0xff);
        b[2] = (byte) ((n >> 16) & 0xff);
        b[3] = (byte) ((n >> 24) & 0xff);
        b[4] = (byte) ((n >> 32) & 0xff);
        b[5] = (byte) ((n >> 40) & 0xff);
        b[6] = (byte) ((n >> 48) & 0xff);
        b[7] = (byte) (n >> 56);
        return b;
    }


    static public long readLong64(byte[] bytes, int pos) {
        ByteBuffer b = ByteBuffer.wrap(bytes, pos, 8);
        b.order(ByteOrder.LITTLE_ENDIAN);
        return b.getLong();
    }
    static public int readInt16(byte[] bytes, int pos) {
        return (((bytes[pos + 1] & 0xFF) << 8) | (0xFF & bytes[pos]));
    }
    static public int readInt32(byte[] bytes, int pos) {
        ByteBuffer b = ByteBuffer.wrap(bytes, pos, 4);
        b.order(ByteOrder.LITTLE_ENDIAN);
        return b.getInt();
    }

    static public byte[] merge(byte[] a,byte[] b){
        if(a == null) return b;
        if(b == null) return a;
        byte[] mer = new byte[a.length + b.length];
        System.arraycopy(a,0,mer,0,a.length);
        System.arraycopy(b,0,mer,a.length,b.length);
        return mer;
    }


     static public byte[] longToBytes(long x) {
         ByteBuffer buffer = ByteBuffer.allocate(8);
         buffer.putLong(0, x);
        return buffer.array();
    }

     static public long bytesToLong(byte[] bytes) {
         ByteBuffer buffer = ByteBuffer.allocate(8);
         buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }


    static public void log(byte[] data){
        if(data != null){
            int ll = 16;
            int off = 0;
            while (off < data.length){
                int end = off + ll;
                end = Math.min(end,data.length);
                StringBuffer sb = new StringBuffer();
                for (int  i = off; i < end ; i++){
                    int val = ((int) data[i]) & 0xff;
                    if (val < 16)
                        sb.append("0");
                    sb.append(Integer.toHexString(val));
                    sb.append("  ");
                }
                sb.append("\n");
                sb.append("\n");
                Log.e("cg_cloud",sb.toString());
                off = end;
            }
        }
    }

}
