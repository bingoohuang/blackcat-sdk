package com.github.bingoohuang.blackcat.sdk.utils;

import com.google.common.base.Joiner;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ProcessExecutor {
    private final Logger log;
    private final String[] toExecute;
    private final long timeoutMillis;

    @Getter private String stdout;
    @Getter private String stderr;

    public ProcessExecutor(Logger log, String[] toExecute, long timeoutMillis) {
        this.log = log;
        this.toExecute = toExecute;
        this.timeoutMillis = timeoutMillis;
    }

    public String call() {
        Process p = null;

        val commandLine = Joiner.on(' ').join(toExecute);

        try {
            p = Runtime.getRuntime().exec(toExecute);

            val stdoutGobbler = new ProcessStreamGobbler(log, commandLine, p.getInputStream(), TYPE.STDOUT);
            val stderrGobbler = new ProcessStreamGobbler(log, commandLine, p.getErrorStream(), TYPE.STDERR);

            val executorService = Executors.newFixedThreadPool(2);
            val stdoutFuture = executorService.submit(stdoutGobbler);
            val stderrFuture = executorService.submit(stderrGobbler);

            waitFor(p, timeoutMillis, TimeUnit.MILLISECONDS);
            executorService.shutdownNow();

            this.stdout = stdoutFuture.get();
            this.stderr = stderrFuture.get();

            return stdout;
        } catch (InterruptedException e) { // thread was interrupted.
            if (p != null) p.destroy();

            Thread.currentThread().interrupt();    // reset interrupted flag
        } catch (Exception e) { // an other error occurred
            if (p != null) p.destroy();
        }

        return null;
    }

    public static boolean waitFor(Process p, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                p.exitValue();
                return true;
            } catch (IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);

        return false;
    }

    public static boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    private enum TYPE {STDOUT, STDERR}

    @AllArgsConstructor
    private static class ProcessStreamGobbler implements Callable<String> {
        private final static String separator = System.getProperty("line.separator");

        private final Logger log;
        private final String commandLine;
        private final InputStream is;
        private final TYPE type;

        @Override
        public String call() {
            val outputStr = new StringBuilder();
            try {
                @Cleanup val isr = new InputStreamReader(is);
                @Cleanup val br = new BufferedReader(isr);

                for (String line; (line = br.readLine()) != null; ) {
                    outputStr.append(line).append(separator);

                    if (type == TYPE.STDOUT)
                        log.info("{} info {}", commandLine, line);
                    else
                        log.warn("{} error {}", commandLine, line);
                }

            } catch (IOException e) {
                log.error("{} io exception {}", commandLine, e.getMessage());
            }

            return outputStr.toString();
        }
    }
}
