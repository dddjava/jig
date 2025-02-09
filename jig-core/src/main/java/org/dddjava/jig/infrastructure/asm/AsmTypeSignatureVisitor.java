package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TypeSignature =
 * visitBaseType
 * | visitTypeVariable
 * | visitArrayType
 * | ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd ) )
 *
 * 例:
 * - {@code List<String>}: {@code Ljava/util/List<Ljava/lang/String;>;}
 * - {@code List<?>}: {@code Ljava/util/List<*>;}
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.9.1-600">JVMS 4.7.9.1-600 FieldSignature</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.9.1-300-B">JVMS 4.7.9.1-300-B ReferenceTypeSignature</a>
 */
class AsmTypeSignatureVisitor extends SignatureVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmTypeSignatureVisitor.class);

    public AsmTypeSignatureVisitor(int api) {
        super(api);
    }

    /**
     *
     * @param name
     * @param arguments visitTypeArgumentのsignatureを処理するAsmTypeSignatureVisitorを出てきた順に保持するためのリスト
     * @param innerClasses visitInnerClassのsignatureを処理するAsmTypeSignatureVisitor
     */
    record ClassType(String name, List<AsmTypeSignatureVisitor> arguments, List<ClassType> innerClasses) {
        ClassType(String name) {
            this(name, new ArrayList<>(), new ArrayList<>());
        }

        void addCurrentTypeArgumentSignatureVisitor(AsmTypeSignatureVisitor typeSignatureVisitor) {
            if (innerClasses.isEmpty()) {
                arguments.add(typeSignatureVisitor);
            } else {
                lastInnerClass().arguments().add(typeSignatureVisitor);
            }
        }

        ClassType lastInnerClass() {
            return innerClasses.get(innerClasses.size() - 1);
        }
    }

    /**
     * ClassTypeSignature
     */
    private ClassType classType = null;

    /**
     * VoidDescriptor or BaseType
     */
    private String baseTypeIdentifier = null;

    /**
     * TypeVariableSignature
     */
    private String typeVariableIdentifier = null;

    /**
     * ArrayTypeSignature
     */
    private AsmTypeSignatureVisitor arrayAsmTypeSignatureVisitor = null; // 配列の時だけ入る

    @Override
    public void visitBaseType(char descriptor) {
        logger.debug("visitBaseType:{}", descriptor);
        baseTypeIdentifier = switch (descriptor) {
            case 'Z' -> "boolean";
            case 'C' -> "char";
            case 'B' -> "byte";
            case 'S' -> "short";
            case 'I' -> "int";
            case 'F' -> "float";
            case 'J' -> "long";
            case 'D' -> "double";
            // methodのResultのときは（BaseTypeではないけど）ここに入ってくる
            case 'V' -> "void";
            default -> throw new IllegalArgumentException("%s is not base type".formatted(descriptor));
        };
    }

    @Override
    public void visitTypeVariable(String name) {
        logger.debug("visitTypeVariable:{}", name);
        this.typeVariableIdentifier = name;
    }

    @Override
    public SignatureVisitor visitArrayType() {
        logger.debug("visitArrayType");
        return arrayAsmTypeSignatureVisitor = new AsmTypeSignatureVisitor(this.api);
    }

    @Override
    public void visitClassType(String name) {
        logger.debug("visitClassType:{}", name);
        classType = new ClassType(name);
    }

    @Override
    public void visitTypeArgument() {
        logger.debug("visitTypeArgument");
        // <?> などで指定された場合。シグネチャでは * となる。
        // 特に処理はしないがこのメソッドが何かのコメントのためにオーバーライドしておく。
        super.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        logger.debug("visitTypeArgument:{}", wildcard);
        // wildcardは '+', '-' or '='.
        // 境界型を使用しない場合は = になる。
        // 一旦考慮しないことにする

        var typeSignatureVisitor = new AsmTypeSignatureVisitor(this.api);
        // この時点でClassTypeがnullの場合は落ちて良い。
        classType.addCurrentTypeArgumentSignatureVisitor(typeSignatureVisitor);
        return typeSignatureVisitor;
    }

    @Override
    public void visitInnerClassType(String name) {
        logger.debug("visitInnerClassType:{}", name);
        classType.innerClasses().add(new ClassType(name));
    }

    @Override
    public void visitEnd() {
        logger.debug("visitEnd");
        super.visitEnd();
    }

    public ParameterizedType generateParameterizedType() {
        if (baseTypeIdentifier != null) {
            return new ParameterizedType(TypeIdentifier.valueOf(baseTypeIdentifier));
        } else if (typeVariableIdentifier != null) {
            return new ParameterizedType(TypeIdentifier.valueOf(typeVariableIdentifier));
        } else if (arrayAsmTypeSignatureVisitor != null) {
            return new ParameterizedType(arrayAsmTypeSignatureVisitor.generateParameterizedType().typeIdentifier().convertArray());
        } else if (classType != null) {

            if (classType.innerClasses().isEmpty()) {
                TypeIdentifier typeIdentifier = TypeIdentifier.valueOf(classType.name());
                var argumentParameterizedTypes = classType.arguments().stream()
                        .map(AsmTypeSignatureVisitor::generateParameterizedType)
                        .toList();
                return new ParameterizedType(typeIdentifier, argumentParameterizedTypes);
            }
            // InnerClassがある場合、このシグネチャの指すクラスは末尾になるので、そのように組み立てる。
            // この場合の途中のParameter型をうまく表現する方法が思い当たらない。とりあえず無視する。
            var innerClassName = classType.innerClasses().stream().map(ClassType::name).collect(Collectors.joining("."));
            TypeIdentifier typeIdentifier = TypeIdentifier.valueOf(classType.name() + "." + innerClassName);
            // 末尾のinnerClassの型パラメタを採用。
            var argumentParameterizedTypes = classType.lastInnerClass().arguments().stream()
                    .map(AsmTypeSignatureVisitor::generateParameterizedType)
                    .toList();
            return new ParameterizedType(typeIdentifier, argumentParameterizedTypes);
        }

        throw new IllegalStateException("想定していたシグネチャではありませんでした。TypeSignatureでないところにAsmTypeSignatureVisitorが使用された？");
    }

    JigTypeArgument typeArgument() {
        logger.debug("typeArgument");
        if (baseTypeIdentifier != null) {
            return JigTypeArgument.primitive(typeVariableIdentifier);
        } else if (typeVariableIdentifier != null) {
            // 型引数に型パラメタが渡されているもの
            return JigTypeArgument.just(typeVariableIdentifier);
        } else if (arrayAsmTypeSignatureVisitor != null) {
            var jigTypeReference = arrayAsmTypeSignatureVisitor.jigTypeReference();
            return JigTypeArgument.just(jigTypeReference.id().convertArray().fullQualifiedName());
        } else if (classType != null) {
            // 型引数がクラスの素直なもの
            // TODO これがさらに型引数を持っているパターンは未対応
            // こっちはInnerClassはありえる？
            return JigTypeArgument.just(classType.name.replace('/', '.'));
        }

        throw new IllegalStateException("JIG内部で不具合が発生しました。報告いただけると幸いです。");
    }

    public JigTypeReference jigTypeReference() {
        if (baseTypeIdentifier != null) {
            return JigTypeReference.fromId(TypeIdentifier.valueOf(baseTypeIdentifier));
        } else if (typeVariableIdentifier != null) {
            return JigTypeReference.fromId(TypeIdentifier.valueOf(typeVariableIdentifier));
        } else if (arrayAsmTypeSignatureVisitor != null) {
            return arrayAsmTypeSignatureVisitor.jigTypeReference().convertArray();
        } else if (classType != null) {
            return new JigTypeReference(
                    TypeIdentifier.fromJvmBinaryName(classType.name()),
                    List.of(), // 型アノテーション未対応
                    classType.arguments().stream()
                            .map(visitor -> visitor.typeArgument())
                            .toList()
            );
        }

        throw new IllegalStateException("JIG内部で不具合が発生しました。報告いただけると幸いです。");
    }
}
