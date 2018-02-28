package jig.infrastructure.jdeps;

import com.sun.tools.jdeps.Main;
import jig.model.jdeps.AnalysisCriteria;
import jig.model.jdeps.RelationAnalyzer;
import jig.model.relation.Relation;
import jig.model.relation.RelationRepository;
import jig.model.relation.Relations;
import jig.model.thing.Name;
import jig.model.thing.Thing;
import jig.model.thing.ThingRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdepsExecutor implements RelationAnalyzer {

    private static final Logger logger = Logger.getLogger(JdepsExecutor.class.getName());

    ThingRepository thingRepository = new OnMemoryThingRepository();
    RelationRepository relationRepository = new OnMemoryRelationRepository();

    @Override
    public Relations analyzeRelations(AnalysisCriteria criteria) {
        String string = analyzeDependency(criteria);
        parse(string);
        return relationRepository.all();
    }

    String analyzeDependency(AnalysisCriteria criteria) {
        try (StringWriter writer = new StringWriter();
             PrintWriter pw = new PrintWriter(writer)) {

            List<String> args = criteria.toJdepsArgs();

            Main.run(args.toArray(new String[args.size()]), pw);

            String resultString = writer.toString();
            logger.info(() -> "jdeps " + args + System.lineSeparator() + resultString);

            return resultString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void parse(String string) {
        String packagePattern = "([\\w.]+)";
        Pattern from = Pattern.compile("^ +" + packagePattern + " \\(.+\\)");
        Pattern to = Pattern.compile("^ +-> " + packagePattern + " ");

        Thing thing = null;
        for (String line : string.split(System.lineSeparator())) {
            Matcher fromMatcher = from.matcher(line);
            if (fromMatcher.find()) {
                Name modelName = new Name(fromMatcher.group(1));
                thing = thingRepository.resolve(modelName);
                continue;
            }

            Matcher toMatcher = to.matcher(line);
            if (toMatcher.find()) {
                if (thing == null) throw new NullPointerException();
                Name modelName = new Name(toMatcher.group(1));
                Relation relation = new Relation(thing, thingRepository.resolve(modelName));
                relationRepository.persist(relation);
                continue;
            }

            logger.warning("skipped: " + line);
        }
    }

}
