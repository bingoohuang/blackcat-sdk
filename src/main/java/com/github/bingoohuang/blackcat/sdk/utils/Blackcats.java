package com.github.bingoohuang.blackcat.sdk.utils;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead.ReqType;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRspHead.RspType;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;

public class Blackcats {
    private static Logger log = LoggerFactory.getLogger(Blackcats.class);


    public static String runShellScript(String shellScript) {
        return executeCommandLine(new String[]{"/bin/bash", "-c", shellScript});
    }

    public static String executeCommandLine(String[] cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            Readable r = new InputStreamReader(p.getInputStream(), UTF_8);
            return CharStreams.toString(r);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static StrBuilder str(String str) {
        return new StrBuilder(str);
    }

    public static StrBuilder str(char ch) {
        StrBuilder strBuilder = new StrBuilder();
        strBuilder.p(ch);
        return strBuilder;
    }

    public static BlackcatReqHead buildHead(BlackcatReqHead.ReqType reqType) {
        return BlackcatReqHead.newBuilder()
                .setHostname(Blackcats.getHostname())
                .setReqType(reqType)
                .setTimestamp(System.currentTimeMillis())
                .build();
    }

    public static List<String> splitLinesWoComments(String text, String commentStart) {
        Splitter splitter = Splitter.on('\n').trimResults().omitEmptyStrings();
        List<String> lines = Lists.newArrayList();

        for (String line : splitter.split(text)) {
            int commentIndex = line.indexOf(commentStart);
            if (commentIndex < 0) {
                lines.add(line);
                continue;
            }

            line = line.substring(0, commentIndex);
            line = StringUtils.trim(line);
            if (StringUtils.isNotEmpty(line)) lines.add(line);
        }

        return lines;
    }

    public static Object parseReq(String packageName, BlackcatReq req) {
        ReqType msgType = req.getBlackcatReqHead().getReqType();

        String simpleName = msgType.toString();
        String className = packageName + "." + simpleName + "Req";
        try {
            Method getMethod = req.getClass().getMethod("get" + simpleName);
            Object methodResult = getMethod.invoke(req);

            Class<?> reqClass = Class.forName(className);
            Constructor<?> ctor = reqClass.getConstructor(
                    BlackcatReqHead.class, methodResult.getClass());
            return ctor.newInstance(req.getBlackcatReqHead(), methodResult);
        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }

    public static Object parseReqBody(BlackcatReq req) {
        ReqType msgType = req.getBlackcatReqHead().getReqType();

        String simpleName = msgType.toString();
        try {
            Method getMethod = req.getClass().getMethod("get" + simpleName);
            return getMethod.invoke(req);

        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }


    public static Object parseRspBody(BlackcatRsp rsp) {
        RspType msgType = rsp.getBlackcatRspHead().getRspType();

        String simpleName = msgType.toString();
        try {
            Method getMethod = rsp.getClass().getMethod("get" + simpleName);
            return getMethod.invoke(rsp);
        } catch (Exception e) {
            log.warn("error", e);
        }

        return null;
    }

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

    public static String format(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
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
        } catch (IOException e) {
            // ignore
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Throwable ex) {
            // ignore
        }

        throw new RuntimeException("unable to get hostname");
    }

    public static String execReadToString(String execCommand) throws IOException {
        Process proc = Runtime.getRuntime().exec(execCommand);
        try (InputStream stream = proc.getInputStream()) {
            try (Scanner s = new Scanner(stream).useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
        }
    }

    public static String decimal(double v) {
        return String.format("%.02f", v);
    }
}
