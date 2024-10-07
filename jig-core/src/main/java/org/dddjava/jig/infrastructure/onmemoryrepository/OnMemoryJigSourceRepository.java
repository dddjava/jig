package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.CommentRepository;
import org.dddjava.jig.application.JigSourceRepository;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.springframework.stereotype.Repository;

@Repository
public class OnMemoryJigSourceRepository implements JigSourceRepository {

    CommentRepository commentRepository;

    public OnMemoryJigSourceRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void registerTextSourceModel(TextSourceModel textSourceModel) {
        for (ClassComment classComment : textSourceModel.classCommentList()) {
            // TODO typeFactsに登録したものを使用するようになれば要らなくなるはず
            commentRepository.register(classComment);
        }

        for (PackageComment packageComment : textSourceModel.packageComments()) {
            commentRepository.register(packageComment);
        }
    }

}
