package com.github.bingoohuang.blackcat.sdk.utils;

import com.google.common.base.Splitter;
import org.n3r.diamond.client.Miner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Blackcats {
    public static String readDiamond(String axis) {
        List<String> parts = Splitter.on('^').splitToList(axis);
        if (parts.size() == 1) {
            String dataId = parts.get(0);
            return new Miner().getString(dataId);
        } else if (parts.size() == 2) {
            String group = parts.get(0);
            String dataId = parts.get(1);
            return new Miner().getStone(group, dataId);
        } else if (parts.size() == 3) {
            String group = parts.get(0);
            String dataId = parts.get(1);
            String key = parts.get(2);
            return new Miner().getProperties(group, dataId).getProperty(key);
        }

        return null;
    }

    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static String humanReadableByteCount(long bytes) {
        return humanReadableByteCount(bytes, true);
    }

    // si 是否是十进制前缀 (SI), SI units and binary units
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
