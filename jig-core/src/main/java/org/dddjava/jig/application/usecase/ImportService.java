package org.dddjava.jig.application.usecase;

import org.dddjava.jig.application.service.DatasourceService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.SpecificationService;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.PackageNameSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.TypeNameSources;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImportService {

    final SpecificationService specificationService;
    final GlossaryService glossaryService;
    final DatasourceService datasourceService;

    public ImportService(SpecificationService specificationService, GlossaryService glossaryService, DatasourceService datasourceService) {
        this.specificationService = specificationService;
        this.datasourceService = datasourceService;
        this.glossaryService = glossaryService;
    }

    public void importSources(ImplementationSources implementationSources, SqlSources sqlSources, TypeNameSources typeNameSources, PackageNameSources packageNameSources) {
        specificationService.importSpecification(implementationSources);

        datasourceService.importDatabaseAccess(sqlSources);

        glossaryService.importJapanese(typeNameSources);
        glossaryService.importJapanese(packageNameSources);
    }
}
