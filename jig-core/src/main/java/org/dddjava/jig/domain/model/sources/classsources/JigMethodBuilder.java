package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.objectweb.asm.Opcodes;

/**
 * メソッドの実装から読み取れること
 */
public class JigMethodBuilder {

    public static MethodDerivation resolveMethodDerivation(JigMethodDeclaration jigMethodDeclaration, int access, boolean isEnum, boolean isRecordComponent) {
        String name = jigMethodDeclaration.name();
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
            if (jigMethodDeclaration.nameAndArgumentSimpleText().equals("values()")) {
                return MethodDerivation.COMPILER_GENERATED;
            } else if (jigMethodDeclaration.nameAndArgumentSimpleText().equals("valueOf(String)")) {
                return MethodDerivation.COMPILER_GENERATED;
            }
        }

        return MethodDerivation.PROGRAMMER;
    }
}
