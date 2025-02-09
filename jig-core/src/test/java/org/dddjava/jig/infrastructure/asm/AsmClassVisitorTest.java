package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeModifier;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.infrastructure.asm.ut.MyClass;
import org.dddjava.jig.infrastructure.asm.ut.MyGenericsMadnessInterface;
import org.dddjava.jig.infrastructure.asm.ut.MyTypeModifierClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsmClassVisitorTest {

    @Test
    void JigTypeDataの取得() throws IOException {
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(MyClass.class.getName()).accept(visitor, 0);

        var typeData = visitor.jigTypeHeader();

        assertEquals("MyClass", typeData.simpleName());
        assertEquals("org.dddjava.jig.infrastructure.asm.ut.MyClass", typeData.fqn());
        assertEquals("MyClass<X, Y>", typeData.simpleNameWithGenerics());

        // MyDeclarationAnnotationForSourceは含まれない
        assertEquals(List.of("MyDeclarationAnnotationForClass", "MyDeclarationAnnotationForRuntime"), typeData.jigTypeAttributeData()
                .declarationAnnotationList().stream().map(JigAnnotationReference::simpleTypeName).toList());

        assertEquals("MySuperClass", typeData.superType().orElseThrow().simpleName());
        assertEquals("MySuperClass<Integer, X, Long>", typeData.superType().orElseThrow().simpleNameWithGenerics());
        assertEquals("org.dddjava.jig.infrastructure.asm.ut.MySuperClass<java.lang.Integer, X, java.lang.Long>", typeData.superType().orElseThrow().fqnWithGenerics());
        assertEquals(List.of("MyInterface", "MyInterface2"), typeData.interfaceTypeList().stream().map(JigTypeReference::simpleName).toList());
        assertEquals(List.of("MyInterface<Y, String>", "MyInterface2<String, Y>"),
                typeData.interfaceTypeList().stream().map(JigTypeReference::simpleNameWithGenerics).toList());
    }

    @Test
    void JigTypeDataの取得_やりすぎばん() throws IOException {
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(MyGenericsMadnessInterface.class.getName()).accept(visitor, 0);

        var typeData = visitor.jigTypeHeader();
        assertEquals("MyGenericsMadnessInterface", typeData.simpleName());
        assertEquals("MyGenericsMadnessInterface<T extends List<Predicate<T>>>", typeData.simpleNameWithGenerics());

        // interfaceのsuperは入ってないとおもってたけどObjectが入っている
        assertEquals("Object", typeData.superType().orElseThrow().simpleName());

        // interfaceのextendsはinterfaceで取れる模様
        assertEquals(List.of("Consumer<Consumer<Function<T, Consumer<Integer>>>>"),
                typeData.interfaceTypeList().stream().map(JigTypeReference::simpleNameWithGenerics).toList());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            MyTypeModifierClass.MyTypeModifierClassSTATIC.class,
            MyTypeModifierClass.MyTypeModifierClassABSTRACT.class,
            MyTypeModifierClass.MyTypeModifierClassFINAL.class,
//            MyTypeModifierClass.MyTypeModifierClassSEALED.class,
//            MyTypeModifierClass.MyTypeModifierClassNON_SEALED.class
    })
    void 修飾子が取得できる(Class<?> target) throws IOException {
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(target.getName()).accept(visitor, 0);

        JigTypeHeader jigTypeHeader = visitor.jigTypeHeader();

        Collection<JigTypeModifier> jigTypeModifiers = jigTypeHeader.jigTypeAttributeData().jigTypeModifiers();
        assertEquals(1, jigTypeModifiers.size());

        assertEquals(
                // クラス名からMyTypeModifierClassを除いたものがenumと一致する
                target.getSimpleName().replace("MyTypeModifierClass", ""),
                jigTypeModifiers.iterator().next().name());
    }

    @Test
    void 多重ネスト_static_inner() throws IOException {
        AsmClassVisitor nestInnerVisitor = new AsmClassVisitor();
        new ClassReader(MyTypeModifierClass.MyTypeModifierClassSTATIC.MyTypeModifierClassSTATICInner.class.getName()).accept(nestInnerVisitor, 0);

        var header = nestInnerVisitor.jigTypeHeader();
        assertFalse(header.jigTypeAttributeData().jigTypeModifiers().contains(JigTypeModifier.STATIC));
    }

    @Test
    void 多重ネスト_static_static() throws IOException {
        AsmClassVisitor nestNestVisitor = new AsmClassVisitor();
        new ClassReader(MyTypeModifierClass.MyTypeModifierClassSTATIC.MyTypeModifierClassSTATICNest.class.getName()).accept(nestNestVisitor, 0);

        var header = nestNestVisitor.jigTypeHeader();
        assertTrue(header.jigTypeAttributeData().jigTypeModifiers().contains(JigTypeModifier.STATIC));
    }
}
