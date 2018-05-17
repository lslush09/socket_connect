package cg.smart.remote_cloud.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * author: shun
 * created on: 2017/9/13 09:40
 * description:
 */
public class CgInputReader extends Reader{
    private InputStream in;
    public CgInputReader(InputStream in) {
        super(in);
        this.in = in;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("can't read char");
    }

    @Override
    public void close() throws IOException {
//        synchronized (lock) {
            if(in != null) {
                in.close();
                in = null;
            }
//        }
    }

    public byte[] readLength(int length,boolean checkHeadr) throws IOException{
        if (length <= 0) {
            return null;
        }
        synchronized (lock){
            if(!__i__isOpen()){
                throw new IOException("InputStream is closed");
            }
            byte[] buffer = new byte[length];
            int index = 0;
            int readCount;
            do {
                readCount = this.in.read(buffer, index, length - index);
                index += readCount;
            } while (readCount != -1 && index < length);

            if (index != length) {
                return null;
            }

            return buffer;

        }
    }

    private boolean __i__isOpen() {
        return this.in != null;
    }
}
