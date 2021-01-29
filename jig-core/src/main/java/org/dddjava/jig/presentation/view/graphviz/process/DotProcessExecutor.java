package org.dddjava.jig.presentation.view.graphviz.process;

import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandResult;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class DotProcessExecutor {
    Logger logger = Logger.getLogger(DotCommandRunner.class.getName());

    public DotCommandResult execute(String... options) {
        String[] command = new String[options.length + 1];
        command[0] = "dot";
        System.arraycopy(options, 0, command, 1, options.length);

        logger.fine("command: " + Arrays.toString(command));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try (Closeable autoShutdown = executorService::shutdown) {
            Process process = new ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start();

            Duration commandTimeout = Duration.ofSeconds(5);

            Future<String> firstLine = executorService.submit(new ProcessOutputStreamHandler(process));
            Future<DotCommandResult> result = executorService.submit(new ProcessResultHandler(process, commandTimeout));

            return result.get().withMessage(firstLine.get());
        } catch (ExecutionException | InterruptedException | IOException e) {
            logger.warning(e.toString());
            return DotCommandResult.failure();
        }
    }
}
