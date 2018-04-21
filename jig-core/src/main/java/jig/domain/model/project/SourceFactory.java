package jig.domain.model.project;

import jig.domain.model.datasource.SqlSources;
import jig.domain.model.japanese.PackageNameSources;
import jig.domain.model.japanese.TypeNameSources;
import jig.domain.model.specification.SpecificationSources;

public interface SourceFactory {
    SpecificationSources getSpecificationSources();

    PackageNameSources getPackageNameSources();

    TypeNameSources getTypeNameSources();

    SqlSources getSqlSources();
}
