package jig.analizer.jdeps;

import com.sun.tools.jdeps.Main;
import jig.domain.model.jdeps.AnalysisCriteria;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

public class JdepsExecutor {

    private static final Logger logger = Logger.getLogger(JdepsExecutor.class.getName());

    private final AnalysisCriteria criteria;

    public JdepsExecutor(AnalysisCriteria criteria) {
        this.criteria = criteria;
    }

    public JdepsResult execute() {
        try (StringWriter writer = new StringWriter();
             PrintWriter pw = new PrintWriter(writer)) {

            List<String> args = criteria.toJdepsArgs();

            Main.run(args.toArray(new String[args.size()]), pw);

            logger.info(() -> "jdeps " + args + System.lineSeparator() + writer.toString());

            return new JdepsResult(writer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
