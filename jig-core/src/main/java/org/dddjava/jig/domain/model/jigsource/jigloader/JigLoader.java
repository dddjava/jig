package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.Alias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;

public interface JigLoader {

    Alias alias(Sources sources, TypeFacts typeFacts);

    Sqls sqls(Sources sources, TypeFacts typeFacts);

    ClassRelations classRelations(TypeFacts typeFacts);

    MethodRelations methodRelations(TypeFacts typeFacts);
}
