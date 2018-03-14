package jig.classlist;

import jig.domain.model.list.ConverterCondition;
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

    List<ConverterCondition> list() {

        List<ConverterCondition> list = new ArrayList<>();
        Relations methods = relationRepository.allMethods();
        for (Relation methodRelation : methods.list()) {
            if (modelKind.correct(methodRelation.from())) {
                ConverterCondition condition = new ConverterCondition(methodRelation, relationRepository, japaneseNameRepository);
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
