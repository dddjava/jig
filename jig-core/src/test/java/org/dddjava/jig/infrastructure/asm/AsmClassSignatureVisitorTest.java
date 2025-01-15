package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsmClassSignatureVisitorTest {

    @Test
    void name() throws IOException {
        Class<Hoge> clz = Hoge.class;
        var map = new HashMap<Class<?>, String>();

        new ClassReader(clz.getName())
                .accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                        map.put(clz, signature);
                    }
                }, 0);

        String actualSignature = map.get(clz);
        assertEquals("Ljava/lang/Object;Ljava/lang/Iterable<Ljava/util/Optional<Ljava/lang/String;>;>;", actualSignature);

        AsmClassSignatureVisitor sut = new AsmClassSignatureVisitor(Opcodes.ASM9);
        new SignatureReader(actualSignature).accept(sut);

        assertEquals("extends [Object] implements [Iterable<Optional<String>>]", sut.simpleText());
    }

    static class Hoge implements Iterable<Optional<String>> {

        @Override
        public Iterator<Optional<String>> iterator() {
            return null;
        }
    }
}