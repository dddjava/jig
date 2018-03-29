package jig.application.service;

import jig.domain.model.datasource.SqlReader;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlSources;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.japanasename.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.specification.ModelReader;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final ModelReader modelReader;
    final SqlReader sqlReader;
    final JapaneseReader japaneseReader;
    final DependencyService dependencyService;
    final JigPaths jigPaths;
    final SqlRepository sqlRepository;

    public AnalyzeService(ModelReader modelReader,
                          SqlReader sqlReader,
                          JapaneseReader japaneseReader,
                          DependencyService dependencyService,
                          JigPaths jigPaths,
                          SqlRepository sqlRepository) {
        this.modelReader = modelReader;
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
        Specifications specifications = readFrom(projectLocation);
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

    public Specifications readFrom(ProjectLocation projectLocation) {
        SpecificationSources specificationSources = jigPaths.getSpecificationSources(projectLocation);
        if (specificationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return modelReader.readFrom(specificationSources);
    }

}
