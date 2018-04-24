package jig.application.usecase;

import jig.application.service.DatasourceService;
import jig.application.service.GlossaryService;
import jig.application.service.SpecificationService;
import jig.domain.model.datasource.SqlSources;
import jig.domain.model.japanese.PackageNameSources;
import jig.domain.model.japanese.TypeNameSources;
import jig.domain.model.specification.SpecificationSources;
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
