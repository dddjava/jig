package jig.infrastructure.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import stub.domain.model.relation.RelationReadTarget;

import java.io.IOException;

public class ClassReaderSandbox {

    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader(RelationReadTarget.class.getName());

        // ConstantPoolの数
        int itemCount = classReader.getItemCount();
        // #0 は使われないので #1 から
        for (int i = 1; i < itemCount; i++) {
            // ConstantPoolのオフセット
            int item = classReader.getItem(i);
            // com.sun.tools.classfile.ConstantPool.CONSTANT_Class = 7
            if (classReader.b[item - 1] == 7) {
                String className = classReader.readUTF8(item, new char[100]);
                Type type = Type.getObjectType(className);
                System.out.println(type.getClassName());
            }
        }

        // NOTE: メソッドの引数と戻り値がCONSTANT_Classでは取れない
    }
}
