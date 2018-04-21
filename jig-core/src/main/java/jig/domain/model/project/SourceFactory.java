package jig.domain.model.project;

import jig.domain.model.datasource.SqlSources;
import jig.domain.model.japanese.PackageNameSources;
import jig.domain.model.japanese.TypeNameSources;
import jig.domain.model.specification.SpecificationSources;
import jig.infrastructure.JigPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

// FIXME
@Component
public class SourceFactory {
    private final JigPaths jigPaths;
    private final Path value;

    public SourceFactory(JigPaths jigPaths, @Value("${project.path}") Path value) {
        this.jigPaths = jigPaths;
        this.value = value;
    }

    public Path toPath() {
        return value;
    }

    public SpecificationSources getSpecificationSources() {
        return jigPaths.getSpecificationSources(this);
    }

    public PackageNameSources getPackageNameSources() {
        return jigPaths.getPackageNameSources(this);
    }

    public TypeNameSources getTypeNameSources() {
        return jigPaths.getTypeNameSources(this);
    }

    public SqlSources getSqlSources() {
        return jigPaths.getSqlSources(this);
    }
}
