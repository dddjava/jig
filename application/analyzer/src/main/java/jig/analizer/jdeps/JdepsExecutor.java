package jig.analizer.jdeps;

import com.sun.tools.jdeps.Main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class JdepsExecutor {

    private static final Logger logger = Logger.getLogger(JdepsExecutor.class.getName());

    final String[] searchPaths;
    final String include;
    final String dependenciesPattern;

    public JdepsExecutor(String include, String dependenciesPattern, String... searchPaths) {
        this.include = include;
        this.dependenciesPattern = dependenciesPattern;
        this.searchPaths = searchPaths;
    }

    public JdepsResult execute() {
        try (StringWriter writer = new StringWriter();
             PrintWriter pw = new PrintWriter(writer)) {

            ArrayList<String> args = new ArrayList<>();
            args.add("-include");
            args.add(include);
            args.add("-e");
            args.add(dependenciesPattern);
            args.addAll(Arrays.asList(searchPaths));

            Main.run(args.toArray(new String[args.size()]), pw);

            logger.info(() -> "jdeps " + args + System.lineSeparator() + writer.toString());

            return new JdepsResult(writer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
