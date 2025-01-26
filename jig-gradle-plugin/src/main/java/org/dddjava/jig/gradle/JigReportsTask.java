package org.dddjava.jig.gradle;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
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
        if (config == null) {
            getLogger().warn("jig-gradle-pluginの設定が取得できません。通常は起こらないはずで、疑われるのはプラグイン側の実装ミスです。続行できないため終了します。");
            return;
        }

        Configuration configuration = new Configuration(config.asProperties(getProject()));

        getLogger().info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", config.propertiesText());

        long startTime = System.currentTimeMillis();
        SourceBasePaths sourceBasePaths = new GradleProject(project).rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourceBasePaths);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        getLogger().info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime, resultLog);
    }

}
