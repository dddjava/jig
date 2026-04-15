package org.dddjava.jig.infrastructure.asm;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;


/**
 * Enumのクラスイニシャライザを解析して定義順を
 *
 * ```
 * 0: new           #1                  // class MyEnum
 * 3: dup
 * 4: ldc           #47                 // String HOGE
 * 6: iconst_0
 * 7: ldc           #48                 // String a
 * 9: invokespecial #50                 // Method "<init>":(Ljava/lang/String;ILjava/lang/CharSequence;)V
 * 12: putstatic     #3                  // Field HOGE:LMyEnum;
 * ```
 */
class AsmEnumClinitMethodVisitor extends MethodVisitor {
    private final ContextClass contextClass;

    record Constant(String name, int ordinal) {
    }

    private final ArrayList<Constant> constants = new ArrayList<>();

    @Nullable
    private String currentName;
    private int currentOrdinal;

    private void resetCurrent() {
        currentName = null;
        currentOrdinal = -1;
    }

    AsmEnumClinitMethodVisitor(ContextClass contextClass, AsmMethodVisitor asmMethodVisitor) {
        super(contextClass.api(), asmMethodVisitor);
        this.contextClass = contextClass;
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW && type.equals(contextClass.classInternalName())) {
            resetCurrent();
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.DUP -> {
                // なにもしない
            }
            case Opcodes.ICONST_0 -> updateCurrentOrdinal(0);
            case Opcodes.ICONST_1 -> updateCurrentOrdinal(1);
            case Opcodes.ICONST_2 -> updateCurrentOrdinal(2);
            case Opcodes.ICONST_3 -> updateCurrentOrdinal(3);
            case Opcodes.ICONST_4 -> updateCurrentOrdinal(4);
            case Opcodes.ICONST_5 -> updateCurrentOrdinal(5);
        }
        super.visitInsn(opcode);
    }

    private void updateCurrentOrdinal(int i) {
        // 定義名直後のiconst_nかbipushがordinal
        if (currentOrdinal == -1) {
            currentOrdinal = i;
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
            updateCurrentOrdinal(operand);
        }
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (currentName == null) {
            // new,dup直後の呼出が定数名
            if (value instanceof String stringValue) {
                currentName = stringValue;
            }
        }
        super.visitLdcInsn(value);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        // コンストラクタの呼び出し
        if (owner.equals(contextClass.classInternalName()) && opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
            if (currentName != null && currentOrdinal != -1) {
                constants.add(new Constant(currentName, currentOrdinal));
                resetCurrent();
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitEnd() {
        // TODO constantを使えるようになんとかする
        super.visitEnd();
    }
}