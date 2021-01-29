package org.dddjava.jig.presentation.view.graphviz.process;

import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandResult;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class ProcessResultHandler implements Callable<DotCommandResult> {
    static final Logger logger = Logger.getLogger(ProcessResultHandler.class.getName());

    Duration duration;
    Process process;

    public ProcessResultHandler(Process process, Duration duration) {
        this.process = process;
        this.duration = duration;
    }

    @Override
    public DotCommandResult call() throws InterruptedException {
        boolean finished = process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            logger.warning("command timeout");
            process.destroy();
            return DotCommandResult.failure();
        }

        int code = process.exitValue();
        if (code == 0) {
            return DotCommandResult.success();
        } else {
            logger.warning("command failed: exit code: " + code);
            return DotCommandResult.failure();
        }
    }
}
