package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSource;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;
import org.dddjava.jig.infrastructure.codeparser.SourceCodeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaparserでJavadocから別名を取得する実装
 */
public class JavaparserAliasReader implements SourceCodeParser {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserAliasReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Override
    public boolean isSupport(PackageInfoSources packageInfoSources) {
        return true;
    }

    @Override
    public boolean isSupport(SourceCodes<? extends SourceCode> sourceCodes) {
        return sourceCodes instanceof JavaSources;
    }

    @Override
    public PackageAliases readPackages(PackageInfoSources nameSources) {
        List<PackageAlias> names = new ArrayList<>();
        for (PackageInfoSource packageInfoSource : nameSources.list()) {
            packageInfoReader.read(packageInfoSource)
                    .ifPresent(names::add);
        }
        return new PackageAliases(names);
    }

    @Override
    public TypeAliases readTypes(SourceCodes<? extends SourceCode> sourceCodes) {
        List<TypeAlias> names = new ArrayList<>();
        List<MethodAlias> methodNames = new ArrayList<>();

        JavaSources sources = (JavaSources) sourceCodes;
        for (JavaSource javaSource : sources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(javaSource);
                TypeAlias typeAlias = typeSourceResult.typeAlias;
                if (typeAlias != null) {
                    names.add(typeAlias);
                }
                methodNames.addAll(typeSourceResult.methodAliases);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", javaSource);
                LOGGER.debug("{}読み取り失敗の詳細", javaSource, e);
            }
        }
        return new TypeAliases(names, methodNames);
    }
}