package jig.gradle;

import jig.infrastructure.LocalProject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class JigImportTask extends DefaultTask {

    ServiceFactory serviceFactory = new ServiceFactory();

    @TaskAction
    void importLocalSources() {
        Project project = getProject();
        LocalProject localProject = serviceFactory.localProject(project);
        serviceFactory.importService().importSources(
                localProject.getSpecificationSources(),
                localProject.getSqlSources(),
                localProject.getTypeNameSources(),
                localProject.getPackageNameSources());
    }

    // TODO もっと適切な方法があると思うのだけど
    static ServiceFactory getServiceFactory(Project project) {
        JigImportTask jigImport = project.getTasks().withType(JigImportTask.class).getByName("jigImport");
        return jigImport.serviceFactory;
    }
}
