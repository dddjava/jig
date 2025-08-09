package org.dddjava.jig.adapter.graphviz;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ProcessExecutor {
    private static final Logger logger = Logger.getLogger(ProcessExecutor.class.getName());
    private final Duration timeout;

    public ProcessExecutor(Duration timeout) {
        this.timeout = timeout;
    }

    public boolean isWin() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase(Locale.ENGLISH).contains("win");
    }

    public ProcessResult execute(String... executeCommand) {
        ArrayList<String> command = new ArrayList<>();
        if (isWin()) {
            command.add("cmd");
            command.add("/C");
        } else {
            command.add("/bin/sh");
            command.add("-c");
        }
        command.add(String.join(" ", executeCommand));

        logger.fine("command: " + command);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try (Closeable ignored = executorService::shutdown) {
            Process process = new ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start();

            Future<String> firstLine = executorService.submit(firstLineReader(process));
            Future<ProcessResult> result = executorService.submit(resultCodeReader(process));

            return result.get().withMessage(firstLine.get());
        } catch (ExecutionException | InterruptedException | IOException e) {
            logger.warning("Execute " + command + " failed: " + e + "\n" +
                    Stream.of(e.getStackTrace())
                            .map(element -> "    " + element.toString())
                            .collect(joining("\n")));
            return ProcessResult.failure();
        }
    }

    /**
     * プロセスの終了を待って結果を返す
     */
    private Callable<ProcessResult> resultCodeReader(Process process) {
        return () -> {
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                logger.warning("command timeout");
                process.destroy();
                return ProcessResult.failureWithTimeout();
            }

            int code = process.exitValue();
            if (code == 0) {
                return ProcessResult.success();
            } else {
                logger.warning("command failed: exit code: " + code);
                return ProcessResult.failure();
            }
        };
    }

    /**
     * 標準出力をロードして1行目だけ返す
     */
    private Callable<String> firstLineReader(Process process) {
        return () -> {
            try (InputStream is = process.getInputStream();
                 Reader r = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(r)) {

                String firstLine = "<none>";
                String line;
                while ((line = br.readLine()) != null) {
                    if (firstLine.equals("<none>")) {
                        firstLine = line;
                    }
                    logger.info(line);
                }
                return firstLine;
            }
        };
    }
}
