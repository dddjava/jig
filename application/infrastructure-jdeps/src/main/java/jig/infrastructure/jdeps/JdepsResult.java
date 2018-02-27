package jig.infrastructure.jdeps;

import jig.model.thing.Name;
import jig.model.thing.Thing;
import jig.model.thing.Things;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdepsResult {

    final String result;

    public JdepsResult(String result) {
        this.result = result;
    }

    public Things toModels() {
        Things things = new Things();

        String packagePattern = "([\\w.]+)";
        Pattern from = Pattern.compile("^ +" + packagePattern + " \\(.+\\)");
        Pattern to = Pattern.compile("^ +-> " + packagePattern + " ");

        Thing thing = null;
        for (String line : result.split(System.lineSeparator())) {
            Matcher fromMatcher = from.matcher(line);
            if (fromMatcher.find()) {
                Name modelName = new Name(fromMatcher.group(1));
                if (things.notExists(modelName)) {
                    things.register(new Thing(modelName));
                }
                thing = things.get(modelName);
                continue;
            }

            Matcher toMatcher = to.matcher(line);
            if (toMatcher.find()) {
                if (thing == null) throw new NullPointerException();
                Name modelName = new Name(toMatcher.group(1));
                if (things.notExists(modelName)) {
                    things.register(new Thing(modelName));
                }
                thing.dependsOn(things.get(modelName));
                continue;
            }

            System.err.println(line);
        }

        return things;
    }
}
