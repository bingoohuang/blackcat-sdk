package com.github.bingoohuang.blackcat.sdk.utils;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n3r.diamond.client.Miner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;

@Slf4j
public class Blackcats {
    public static String runShellScript(String shellScript) {
        return executeCommandLine(new String[]{"/bin/bash", "-c", shellScript});
    }

    @SneakyThrows
    public static String executeCommandLine(String[] cmd) {
        val p = Runtime.getRuntime().exec(cmd);
        p.waitFor();

        @Cleanup val r = new InputStreamReader(p.getInputStream(), UTF_8);
        return CharStreams.toString(r);
    }

    public static BlackcatReqHead buildHead(BlackcatReqHead.ReqType reqType) {
        return BlackcatReqHead.newBuilder()
                .setHostname(Blackcats.getHostname())
                .setReqType(reqType)
                .setTimestamp(System.currentTimeMillis())
                .build();
    }

    public static Object parseReq(String packageName, BlackcatReq req) {
        val msgType = req.getBlackcatReqHead().getReqType();

        val simpleName = msgType.toString();
        val className = packageName + "." + simpleName + "Req";
        try {
            val getMethod = req.getClass().getMethod("get" + simpleName);
            val methodResult = getMethod.invoke(req);

            val reqClass = Class.forName(className);
            val ctor = reqClass.getConstructor(
                    BlackcatReqHead.class, methodResult.getClass());
            return ctor.newInstance(req.getBlackcatReqHead(), methodResult);
        } catch (ClassNotFoundException e) {
            log.debug("ClassNotFoundException:{}", e.getMessage());
        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }

    public static Object parseReqBody(BlackcatReq req) {
        val msgType = req.getBlackcatReqHead().getReqType();

        val simpleName = msgType.toString();
        try {
            val getMethod = req.getClass().getMethod("get" + simpleName);
            return getMethod.invoke(req);

        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }


    public static Object parseRspBody(BlackcatRsp rsp) {
        val msgType = rsp.getBlackcatRspHead().getRspType();

        val simpleName = msgType.toString();
        try {
            val getMethod = rsp.getClass().getMethod("get" + simpleName);
            return getMethod.invoke(rsp);
        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }

    public static String readDiamond(String axis) {
        val parts = Splitter.on('^').splitToList(axis);
        if (parts.size() == 1) {
            val dataId = parts.get(0);
            return new Miner().getString(dataId);
        }

        if (parts.size() == 2) {
            val group = parts.get(0);
            val dataId = parts.get(1);
            return new Miner().getStone(group, dataId);
        }

        if (parts.size() == 3) {
            val group = parts.get(0);
            val dataId = parts.get(1);
            val key = parts.get(2);
            return new Miner().getProperties(group, dataId).getProperty(key);
        }

        return null;
    }

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(long time) {
        return new DateTime(time).toString(formatter);
    }

    public static String now() {
        return DateTime.now().toString(formatter);
    }

    public static void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static String prettyBytes(long bytes) {
        return prettyBytes(bytes, false);
    }

    // si 是否是十进制前缀 (SI), SI units and binary units
    public static String prettyBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getHostname() {
        try {
            return StringUtils.trim(execReadToString("hostname"));
        } catch (Throwable ex) {
            // ignore
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Throwable ex) {
            // ignore
        }

        throw new RuntimeException("unable to get hostname");
    }

    @SneakyThrows
    public static String execReadToString(String execCommand) {
        val proc = Runtime.getRuntime().exec(execCommand);
        @Cleanup val stream = proc.getInputStream();
        @Cleanup val scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public static String decimal(double v) {
        return String.format("%.02f", v);
    }

    public static ClassLoader getClassLoader() {
        return MoreObjects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                Blackcats.class.getClassLoader());
    }

    public static InputStream classpathInputStream(String pathname, boolean silent) {
        val is = classpathInputStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    public static InputStream classpathInputStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }

    public static boolean hasSpring = classExists("org.springframework.context.ApplicationContext");
    public static boolean hasDiamond = classExists("org.n3r.diamond.client.Miner");

    public static boolean classExists(String className) {
        try {
            Class.forName(className, false, Blackcats.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
