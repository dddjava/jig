package org.dddjava.jig.presentation.view.graphviz.process;

import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessExecutor {
    Logger logger = Logger.getLogger(DotCommandRunner.class.getName());

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

            // TODO タイムアウト時間を設定可能にする
            Duration commandTimeout = Duration.ofSeconds(10);

            Future<String> firstLine = executorService.submit(firstLineReader(process));
            Future<ProcessResult> result = executorService.submit(resultCodeReader(process, commandTimeout));

            return result.get().withMessage(firstLine.get());
        } catch (ExecutionException | InterruptedException | IOException e) {
            logger.warning("Execute " + command + " failed: " + e.toString() + "\n" +
                    Stream.of(e.getStackTrace())
                            .map(element -> "    " + element.toString())
                            .collect(Collectors.joining("\n")));
            return ProcessResult.failure();
        }
    }

    /**
     * プロセスの終了を待って結果を返す
     */
    private Callable<ProcessResult> resultCodeReader(Process process, Duration commandTimeout) {
        return () -> {
            boolean finished = process.waitFor(commandTimeout.toMillis(), TimeUnit.MILLISECONDS);
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
