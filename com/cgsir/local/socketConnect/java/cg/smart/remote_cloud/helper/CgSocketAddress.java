package cg.smart.remote_cloud.helper;

import java.net.InetSocketAddress;

import cg.smart.remote_cloud.util.StringValidation;

/**
 * author: shun
 * created on: 2017/9/12 18:07
 * description:
 */
public class CgSocketAddress {
    public static final int DefaultConnectionTimeout = 1000 * 5;

    public CgSocketAddress() {
        this(null,null);
    }

    public CgSocketAddress(String remoteIP, String remotePort) {
        this(remoteIP,remotePort,DefaultConnectionTimeout);
    }

    public CgSocketAddress(String remoteIP, String remotePort, int connectionTimeout) {
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
        this.connectionTimeout = connectionTimeout;
    }


    //检验ip,端口和超时时间是否合法
    public void checkValidation() {
        if (!StringValidation.validateRegex(getRemoteIP(), StringValidation.RegexIP)) {
            throw new IllegalArgumentException("we need a correct remote IP to connect. Current is " + getRemoteIP());
        }

        if (!StringValidation.validateRegex(getRemotePort(), StringValidation.RegexPort)) {
            throw new IllegalArgumentException("we need a correct remote port to connect. Current is " + getRemotePort());
        }

        if (getConnectionTimeout() < 0) {
            throw new IllegalArgumentException("we need connectionTimeout > 0. Current is " + getConnectionTimeout());
        }
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(getRemoteIP(), getRemotePortIntegerValue());
    }

    public int getRemotePortIntegerValue() {
        if (getRemotePort() == null) {
            return 0;
        }

        return Integer.valueOf(getRemotePort());
    }
    /**
     * 远程IP
     */
    private String remoteIP;
    public CgSocketAddress setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
        return this;
    }
    public String getRemoteIP() {
        return this.remoteIP;
    }

    /**
     * 远程端口
     */
    private String remotePort;
    public CgSocketAddress setRemotePort(String remotePort) {
        this.remotePort = remotePort;
        return this;
    }
    public String getRemotePort() {
        return this.remotePort;
    }

    /**
     * 连接超时时间
     */
    private int connectionTimeout;
    public CgSocketAddress setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }
}
