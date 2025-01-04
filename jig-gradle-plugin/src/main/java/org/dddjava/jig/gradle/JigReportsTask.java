package org.dddjava.jig.gradle;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.util.List;
import java.util.stream.Collectors;

public class JigReportsTask extends DefaultTask {

    @TaskAction
    void outputReports() {
        Project project = getProject();
        JigConfig config = project.getExtensions().findByType(JigConfig.class);

        Configuration configuration = new Configuration(config.asProperties(getProject()));

        getLogger().info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", config.propertiesText());

        long startTime = System.currentTimeMillis();
        SourcePaths sourcePaths = new GradleProject(project).rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourcePaths);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        getLogger().info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime, resultLog);
    }

}
