package com.ronald8192.djifixgui;

public enum OSType {
    LINUX("_linux"),
    MAC("_mac"),
    WINDOWS(".exe"),
    NOT_SUPPORTED("");

    private String binSuffix;

    OSType(String binSuffix){
        this.binSuffix = binSuffix;
    }

    public String getBinSuffix() {
        return binSuffix;
    }
}