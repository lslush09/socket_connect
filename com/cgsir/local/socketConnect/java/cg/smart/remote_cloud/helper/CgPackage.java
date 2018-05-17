package cg.smart.remote_cloud.helper;

/**
 * author: shun
 * created on: 2017/9/12 19:13
 * description:
 */
public interface CgPackage{
    byte[] getHeadByte();
    byte[] getBodyByte(String charName);


    interface ParsePackageDelegate{
            int headLength();
            int dataLength(byte[] head);
    }

     CgPackage empty = new CgPackage() {
        @Override
        public byte[] getHeadByte() {
            return new byte[0];
        }

        @Override
        public byte[] getBodyByte(String charName) {
            return new byte[0];
        }
    };
}
