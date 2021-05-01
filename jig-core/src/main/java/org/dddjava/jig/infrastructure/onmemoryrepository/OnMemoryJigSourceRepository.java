package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigsource.jigfactory.AliasRegisterResult;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.jigsource.jigreader.CommentRepository;
import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class OnMemoryJigSourceRepository implements JigSourceRepository {
    static Logger logger = LoggerFactory.getLogger(OnMemoryJigSourceRepository.class);

    CommentRepository commentRepository;
    TypeFacts typeFacts = new TypeFacts(Collections.emptyList());
    Sqls sqls;

    public OnMemoryJigSourceRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void registerSqls(Sqls sqls) {
        this.sqls = sqls;
    }

    @Override
    public void registerTypeFact(TypeFacts typeFacts) {
        this.typeFacts = typeFacts;
    }

    @Override
    public void registerPackageComment(PackageComment packageComment) {
        typeFacts.registerPackageAlias(packageComment);
        commentRepository.register(packageComment);
    }

    @Override
    public AliasRegisterResult registerClassComment(ClassComment classComment) {
        AliasRegisterResult aliasRegisterResult = typeFacts.registerTypeAlias(classComment);
        // TODO typeFactsに登録したものを使用するようになれば要らなくなるはず
        commentRepository.register(classComment);

        if (aliasRegisterResult != AliasRegisterResult.成功) {
            logger.warn("{} のコメント登録が {} です。処理は続行します。",
                    classComment.typeIdentifier().fullQualifiedName(), aliasRegisterResult);
        }
        return aliasRegisterResult;
    }

    @Override
    public void registerMethodComment(MethodComment methodComment) {
        AliasRegisterResult aliasRegisterResult = typeFacts.registerMethodAlias(methodComment);

        if (aliasRegisterResult != AliasRegisterResult.成功) {
            logger.warn("{} のコメント登録が {} です。処理は続行します。",
                    methodComment.methodIdentifier().asText(), aliasRegisterResult);
        }
    }

    @Override
    public TypeFacts allTypeFacts() {
        return typeFacts;
    }

    @Override
    public Sqls sqls() {
        return sqls;
    }
}
