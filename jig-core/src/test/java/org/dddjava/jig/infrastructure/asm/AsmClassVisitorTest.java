package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationInstance;
import org.dddjava.jig.domain.model.data.types.JigBaseTypeData;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeModifier;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                .declarationAnnotationList().stream().map(JigAnnotationInstance::simpleTypeName).toList());

        assertEquals("MySuperClass", typeData.superType().orElseThrow().simpleName());
        assertEquals("MySuperClass<Integer, X, Long>", typeData.superType().orElseThrow().simpleNameWithGenerics());
        assertEquals("org.dddjava.jig.infrastructure.asm.ut.MySuperClass<java.lang.Integer, X, java.lang.Long>", typeData.superType().orElseThrow().fqnWithGenerics());
        assertEquals(List.of("MyInterface", "MyInterface2"), typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleName).toList());
        assertEquals(List.of("MyInterface<Y, String>", "MyInterface2<String, Y>"),
                typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleNameWithGenerics).toList());
    }

    @Test
    void JigTypeDataの取得_やりすぎばん() throws IOException {
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(MyGenericsMadnessInterface.class.getName()).accept(visitor, 0);

        var typeData = visitor.jigTypeHeader();
        assertEquals("MyGenericsMadnessInterface", typeData.simpleName());
        assertEquals("MyGenericsMadnessInterface<T extends List>", typeData.simpleNameWithGenerics());
        // とれるようにしたいけどとりあえずはいいかなと
        //assertEquals("MyGenericsMadnessInterface<T extends List<Predicate<T>>", typeData.simpleNameWithGenerics());

        // interfaceのsuperは入ってないとおもってたけどObjectが入っている
        assertEquals("Object", typeData.superType().orElseThrow().simpleName());

        // interfaceのextendsはinterfaceで取れる模様
        assertEquals(List.of("Consumer<Consumer>"),
                typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleNameWithGenerics).toList());
    }


    @ParameterizedTest
    @ValueSource(classes = {
//            MyTypeModifierClass.MyTypeModifierClassSTATIC.class,
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
}
