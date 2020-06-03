package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.CalleeMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.CallerMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;

import java.util.ArrayList;
import java.util.List;

public class RelationsFactory {

    public static ClassRelations createClassRelations(TypeFacts typeFacts) {
        List<ClassRelation> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            TypeIdentifier form = typeFact.typeIdentifier();
            for (TypeIdentifier to : typeFact.useTypes().list()) {
                ClassRelation classRelation = new ClassRelation(form, to);
                if (classRelation.selfRelation()) continue;
                list.add(classRelation);
            }
        }
        return new ClassRelations(list);
    }

    public static MethodRelations createMethodRelations(TypeFacts typeFacts) {
        ArrayList<MethodRelation> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            for (MethodFact methodFact : typeFact.allMethodFacts()) {
                CallerMethod callerMethod = new CallerMethod(methodFact.methodDeclaration);
                for (MethodDeclaration usingMethod : methodFact.methodDepend().usingMethods().methodDeclarations().list()) {
                    list.add(new MethodRelation(callerMethod, new CalleeMethod(usingMethod)));
                }
            }
        }
        return new MethodRelations(list);
    }
}
