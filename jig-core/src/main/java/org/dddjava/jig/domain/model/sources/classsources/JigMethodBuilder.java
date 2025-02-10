package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * メソッドの実装から読み取れること
 */
public class JigMethodBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JigMethodBuilder.class);

    private final JigMethodDeclaration jigMethodDeclaration;
    private final MethodDeclaration methodDeclaration;
    private final MethodDerivation methodDerivation;
    private Term term = null;

    public JigMethodBuilder(JigMethodDeclaration jigMethodDeclaration, MethodDeclaration methodDeclaration, MethodDerivation methodDerivation) {
        this.jigMethodDeclaration = jigMethodDeclaration;
        this.methodDeclaration = methodDeclaration;
        this.methodDerivation = methodDerivation;
    }

    public static MethodDerivation resolveMethodDerivation(MethodDeclaration methodDeclaration, int access, boolean isEnum, boolean isRecordComponent) {
        MethodSignature methodSignature = methodDeclaration.methodSignature();
        String name = methodSignature.methodName();
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return MethodDerivation.CONSTRUCTOR;
        }

        if ((access & Opcodes.ACC_BRIDGE) != 0 || (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return MethodDerivation.COMPILER_GENERATED;
        }

        if (isRecordComponent) {
            return MethodDerivation.RECORD_COMPONENT;
        }

        if (isEnum && (access & Opcodes.ACC_STATIC) != 0) {
            // enumで生成されるstaticメソッド2つをコンパイラ生成として扱う
            if (methodSignature.isSame(new MethodSignature("values"))) {
                return MethodDerivation.COMPILER_GENERATED;
            } else {
                if (methodSignature.isSame(new MethodSignature("valueOf", TypeIdentifier.from(String.class)))) {
                    return MethodDerivation.COMPILER_GENERATED;
                }
            }
        }

        return MethodDerivation.PROGRAMMER;
    }

    public static JigMethodBuilder builder(JigMethodDeclaration jigMethodDeclaration,
                                           int access,
                                           MethodDeclaration methodDeclaration,
                                           boolean isEnum, boolean isRecordComponent) {
        MethodDerivation methodDerivation = resolveMethodDerivation(methodDeclaration, access, isEnum, isRecordComponent);
        return new JigMethodBuilder(jigMethodDeclaration, methodDeclaration, methodDerivation);
    }

    public JigMethod build() {
        return new JigMethod(jigMethodDeclaration, methodDeclaration, methodDerivation, term);
    }

    public MethodIdentifier methodIdentifier() {
        return methodDeclaration.identifier();
    }

    public void registerMethodTerm(Term term) {
        this.term = term;
    }
}
