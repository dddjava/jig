package jig.application.service;

import jig.domain.model.datasource.SqlReader;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlSources;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.japanese.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final SpecificationService specificationService;
    final SqlReader sqlReader;
    final JapaneseReader japaneseReader;
    final DependencyService dependencyService;
    final JigPaths jigPaths;
    final SqlRepository sqlRepository;

    public AnalyzeService(SpecificationService specificationService,
                          SqlReader sqlReader,
                          JapaneseReader japaneseReader,
                          DependencyService dependencyService,
                          JigPaths jigPaths,
                          SqlRepository sqlRepository) {
        this.specificationService = specificationService;
        this.sqlReader = sqlReader;
        this.japaneseReader = japaneseReader;
        this.dependencyService = dependencyService;
        this.jigPaths = jigPaths;
        this.sqlRepository = sqlRepository;
    }

    public PackageDependencies packageDependencies(ProjectLocation projectLocation) {
        importSpecification(projectLocation);
        importJapanese(projectLocation);
        return dependencyService.packageDependencies();
    }

    public void importProject(ProjectLocation projectLocation) {
        importSpecification(projectLocation);
        importDatabaseAccess(projectLocation);
        importJapanese(projectLocation);
    }

    public void importSpecification(ProjectLocation projectLocation) {
        SpecificationSources specificationSources = jigPaths.getSpecificationSources(projectLocation);
        Specifications specifications = specificationService.specification(specificationSources);

        dependencyService.register(specifications);
    }

    public void importDatabaseAccess(ProjectLocation projectLocation) {
        SqlSources sqlSources = jigPaths.getSqlSources(projectLocation);
        Sqls sqls = sqlReader.readFrom(sqlSources);
        sqlRepository.register(sqls);
    }

    public void importJapanese(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }
}
