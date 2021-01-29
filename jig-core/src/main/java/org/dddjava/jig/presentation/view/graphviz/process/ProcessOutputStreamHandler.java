package org.dddjava.jig.presentation.view.graphviz.process;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

class ProcessOutputStreamHandler implements Callable<String> {
    Logger logger = Logger.getLogger(ProcessOutputStreamHandler.class.getName());

    Process process;

    public ProcessOutputStreamHandler(Process process) {
        this.process = process;
    }

    @Override
    public String call() throws IOException {
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
    }
}
