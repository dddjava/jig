package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigsource.jigreader.CommentRepository;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class AliasService {

    final CommentRepository commentRepository;

    public AliasService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * パッケージ別名を取得する
     */
    public PackageComment packageAliasOf(PackageIdentifier packageIdentifier) {
        return commentRepository.get(packageIdentifier);
    }

    /**
     * 型別名を取得する
     */
    public ClassComment typeAliasOf(TypeIdentifier typeIdentifier) {
        return commentRepository.get(typeIdentifier);
    }
}
