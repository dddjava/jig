package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigloaded.alias.Alias;
import org.dddjava.jig.domain.model.jigloaded.datasource.Sqls;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;
import org.dddjava.jig.domain.model.jigsource.file.Sources;

public interface JigLoader {

    Alias alias(Sources sources, TypeByteCodes typeByteCodes);

    Sqls sqls(Sources sources, TypeByteCodes typeByteCodes);

    ClassRelations classRelations(TypeByteCodes typeByteCodes);

    MethodRelations methodRelations(TypeByteCodes typeByteCodes);
}
