package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.objectweb.asm.Opcodes;

class AsmUtils {

    static JigMemberVisibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return JigMemberVisibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return JigMemberVisibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return JigMemberVisibility.PRIVATE;
        return JigMemberVisibility.PACKAGE;
    }

    static JigMemberOwnership jigMemberOwnership(int access) {
        return ((access & Opcodes.ACC_STATIC) == 0) ? JigMemberOwnership.INSTANCE : JigMemberOwnership.CLASS;
    }
}
