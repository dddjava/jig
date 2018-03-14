package jig.classlist;

import jig.domain.model.list.MethodRelationNavigator;
import jig.domain.model.list.kind.ModelKind;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListWriter implements InitializingBean {

    @Value("${output.list.type}")
    String listType;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    List<MethodRelationNavigator> list() {

        List<MethodRelationNavigator> list = new ArrayList<>();
        Relations methods = relationRepository.allMethods();
        for (Relation methodRelation : methods.list()) {
            if (modelKind.correct(methodRelation.from())) {
                MethodRelationNavigator condition = new MethodRelationNavigator(methodRelation, relationRepository, japaneseNameRepository);
                list.add(condition);
            }
        }

        return list;
    }

    ModelKind modelKind;

    @Override
    public void afterPropertiesSet() {
        modelKind = ModelKind.valueOf(this.listType.toUpperCase());
    }
}
