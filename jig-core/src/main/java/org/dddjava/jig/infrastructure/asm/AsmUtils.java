package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

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

    /**
     * TypeのdescriptorをTypeIdに変換する。
     * Type以外のdescriptorでは例外が発生する。
     */
    static TypeId typeDescriptorToTypeId(String typeDescriptor) {
        Type type = Type.getType(typeDescriptor);
        return type2TypeId(type);
    }

    public static TypeId type2TypeId(Type typeValue) {
        return TypeId.valueOf(typeValue.getClassName());
    }
}
