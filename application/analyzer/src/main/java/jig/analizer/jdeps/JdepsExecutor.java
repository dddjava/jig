package jig.analizer.jdeps;

import com.sun.tools.jdeps.Main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class JdepsExecutor {

    final String[] jarPaths;
    final String include;
    final String dependenciesPattern;

    public JdepsExecutor(String include, String dependenciesPattern, String... jarPaths) {
        this.jarPaths = jarPaths;
        this.include = include;
        this.dependenciesPattern = dependenciesPattern;
    }

    public JdepsResult execute() {
        try (StringWriter writer = new StringWriter();
             PrintWriter pw = new PrintWriter(writer)) {

            ArrayList<String> args = new ArrayList<>();
            args.add("-include");
            args.add(include);
            args.add("-e");
            args.add(dependenciesPattern);
            args.addAll(Arrays.asList(jarPaths));

            Main.run(args.toArray(new String[args.size()]), pw);

            return new JdepsResult(writer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
