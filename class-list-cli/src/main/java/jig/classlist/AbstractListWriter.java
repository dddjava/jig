package jig.classlist;

import jig.domain.model.list.MethodRelationNavigator;
import jig.domain.model.list.kind.ModelKind;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.thing.Names;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListWriter implements InitializingBean {

    @Value("${output.list.type}")
    String listType;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    List<MethodRelationNavigator> list() {

        List<MethodRelationNavigator> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        Relations methods = relationRepository.methodsOf(names);
        for (Relation methodRelation : methods.list()) {
            MethodRelationNavigator condition = new MethodRelationNavigator(methodRelation, relationRepository, japaneseNameRepository);
            list.add(condition);
        }

        return list;
    }

    ModelKind modelKind;
    private Tag tag;

    @Override
    public void afterPropertiesSet() {
        modelKind = ModelKind.valueOf(listType.toUpperCase());
        tag = Tag.valueOf(listType.toUpperCase());
    }
}
