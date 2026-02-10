package org.dddjava.jig.infrastructure.asm;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.filesystem.ClassFilePaths;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * ASMを使用して*.classファイルを読むエントリーポイント
 *
 * ASMの設定などはこのクラスで行う。
 */
@Repository
public class AsmClassSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmClassSourceReader.class);

    private static volatile boolean loggedSkippedModules = false;

    private final Counter counter = Metrics.counter("jig.analysis.class.count");

    public Collection<ClassDeclaration> readClasses(ClassFilePaths classFilePaths) {
        return classFilePaths.values().stream()
                .map(classFile -> classDeclaration(classFile))
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<ClassDeclaration> classDeclaration(Path path) {
        // そのまま読ませると予期しないエラーになりがちなのでスキップしておく。package-infoはsuperがObjectだけど、module-infoはsuperが無しでnullになるとか。
        if (path.endsWith(Path.of("module-info.class")) || path.endsWith(Path.of("package-info.class"))) {
            if (!loggedSkippedModules) {
                logger.info("package-info や module-info の情報（アノテーションなど）は現在読み取っていません。");
                loggedSkippedModules = true;
            }
            return Optional.empty();
        }

        return readClassBytes(path)
                .flatMap(this::getClassDeclaration);
    }

    private Optional<ClassDeclaration> getClassDeclaration(byte[] classBytes) {
        try {
            counter.increment();
            AsmClassVisitor asmClassVisitor = new AsmClassVisitor();

            ClassReader classReader = new ClassReader(classBytes);
            classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);

            return Optional.of(asmClassVisitor.classDeclaration());
        } catch (Exception e) {
            logger.warn("クラスデータの読み取りに失敗しました。スキップして続行します。", e);
            return Optional.empty();
        }
    }

    private static Optional<byte[]> readClassBytes(Path path) {
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.warn("クラスファイルの読み取りに失敗しました。スキップして続行します。", e);
            return Optional.empty();
        }
    }
}