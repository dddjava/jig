package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
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
