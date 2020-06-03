package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloaded.relation.method.CalleeMethod;
import org.dddjava.jig.domain.model.jigloaded.relation.method.CallerMethod;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigsource.file.binary.MethodByteCode;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

public class RelationsFactory {

    public static ClassRelations createClassRelations(TypeByteCodes typeByteCodes) {
        List<ClassRelation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier form = typeByteCode.typeIdentifier();
            for (TypeIdentifier to : typeByteCode.useTypes().list()) {
                ClassRelation classRelation = new ClassRelation(form, to);
                if (classRelation.selfRelation()) continue;
                list.add(classRelation);
            }
        }
        return new ClassRelations(list);
    }

    public static MethodRelations createMethodRelations(TypeByteCodes typeByteCodes) {
        ArrayList<MethodRelation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.methodByteCodes()) {
                CallerMethod callerMethod = new CallerMethod(methodByteCode.methodDeclaration);
                for (MethodDeclaration usingMethod : methodByteCode.methodDepend().usingMethods().methodDeclarations().list()) {
                    list.add(new MethodRelation(callerMethod, new CalleeMethod(usingMethod)));
                }
            }
        }
        return new MethodRelations(list);
    }
}
