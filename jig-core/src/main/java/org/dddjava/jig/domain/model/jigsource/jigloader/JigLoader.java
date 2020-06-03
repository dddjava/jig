package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.alias.Alias;
import org.dddjava.jig.domain.model.jigmodel.datasource.Sqls;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;

public interface JigLoader {

    Alias alias(Sources sources, TypeByteCodes typeByteCodes);

    Sqls sqls(Sources sources, TypeByteCodes typeByteCodes);

    ClassRelations classRelations(TypeByteCodes typeByteCodes);

    MethodRelations methodRelations(TypeByteCodes typeByteCodes);
}
