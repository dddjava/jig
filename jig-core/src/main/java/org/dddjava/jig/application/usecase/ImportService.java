package org.dddjava.jig.application.usecase;

import org.dddjava.jig.application.service.DatasourceService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.SpecificationService;
import org.dddjava.jig.domain.model.datasource.SqlSources;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.dddjava.jig.domain.model.specification.SpecificationSources;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.SpecificationService;
import org.dddjava.jig.domain.model.datasource.SqlSources;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.dddjava.jig.domain.model.specification.SpecificationSources;
import org.springframework.stereotype.Service;

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

    public void importSources(SpecificationSources specificationSources, SqlSources sqlSources, TypeNameSources typeNameSources, PackageNameSources packageNameSources) {
        specificationService.importSpecification(specificationSources);

        datasourceService.importDatabaseAccess(sqlSources);

        glossaryService.importJapanese(typeNameSources);
        glossaryService.importJapanese(packageNameSources);
    }
}
