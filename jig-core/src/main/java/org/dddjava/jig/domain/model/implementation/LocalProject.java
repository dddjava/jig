package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSources;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.PackageNameSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.TypeNameSources;

public interface LocalProject {
    ByteCodeSources getByteCodeSources();

    SqlSources getSqlSources();

    PackageNameSources getPackageNameSources();

    TypeNameSources getTypeNameSources();
}
