package jig.analizer.jdeps;

import jig.domain.model.dependency.Model;
import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.Models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdepsResult {

    final String result;

    public JdepsResult(String result) {
        this.result = result;
    }

    public Models toModels() {
        Models models = new Models();

        String packagePattern = "([\\w.]+)";
        Pattern from = Pattern.compile("^ +" + packagePattern + " \\(.+\\)");
        Pattern to = Pattern.compile("^ +-> " + packagePattern + " ");

        Model model = null;
        for (String line : result.split(System.lineSeparator())) {
            Matcher fromMatcher = from.matcher(line);
            if (fromMatcher.find()) {
                FullQualifiedName modelName = new FullQualifiedName(fromMatcher.group(1));
                if (models.notExists(modelName)) {
                    models.register(new Model(modelName));
                }
                model = models.get(modelName);
                continue;
            }

            Matcher toMatcher = to.matcher(line);
            if (toMatcher.find()) {
                if (model == null) throw new NullPointerException();
                FullQualifiedName modelName = new FullQualifiedName(toMatcher.group(1));
                if (models.notExists(modelName)) {
                    models.register(new Model(modelName));
                }
                model.dependsOn(models.get(modelName));
                continue;
            }

            System.err.println(line);
        }

        return models;
    }
}
