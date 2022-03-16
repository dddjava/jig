package org.dddjava.jig.infrastructure.asm.learning;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stub.domain.model.category.ParameterizedEnum;
import stub.domain.model.category.RichEnum;
import stub.domain.model.category.SimpleEnum;

public class AsmEnumTest {
    static Logger logger = LoggerFactory.getLogger(AsmEnumTest.class);


    @ValueSource(classes = {
            SimpleEnum.class,
            ParameterizedEnum.class,
            RichEnum.class,
    })
    @ParameterizedTest
    void test(Class<?> clz) throws Exception {
        logger.info(clz.getName());

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                logger.info("access={}, name={}, descriptor={}, signature={}, exceptions={}", access, name, descriptor, signature, exceptions);

                return new MethodVisitor(api) {
                    @Override
                    public void visitParameter(String name1, int access1) {
                        // これでMethodParametersを読むには以下の条件が必要
                        // - コンパイルオプションに -parameters が入っている
                        // - ClassReaderでSKIP_DEBUGが指定されていない
                        logger.info(" name={} access={}", name1, access1);

                        // enumのコンストラクタは 第一引数 $enum$name 第二引数 $enum$ordinal になる
                        // 実装したコンストラクタは第三引数以降
                        super.visitParameter(name1, access1);
                    }
                };
            }
        };

        ClassReader classReader = new ClassReader(clz.getName());
        classReader.accept(visitor, 0);//ClassReader.SKIP_DEBUG);
    }
}
