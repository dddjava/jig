package org.dddjava.jig.infrastructure.codeparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.AliasReader;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageAliases;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.TypeAliases;
import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SourceCodeJapaneseReader implements AliasReader {

    List<SourceCodeParser> parsers;

    public SourceCodeJapaneseReader(List<SourceCodeParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public PackageAliases readPackages(PackageInfoSources packageInfoSources) {
        Optional<SourceCodeParser> codeParser = parsers.stream()
                .filter(parser -> parser.isSupport(packageInfoSources))
                .findFirst();
        return codeParser
                .map(parser -> parser.readPackages(packageInfoSources))
                .orElseGet(() -> new PackageAliases(Collections.emptyList()));
    }

    @Override
    public TypeAliases readTypes(SourceCodes<? extends SourceCode> sourceCodes) {
        Optional<SourceCodeParser> codeParser = parsers.stream()
                .filter(parser -> parser.isSupport(sourceCodes))
                .findFirst();
        return codeParser
                .map(parser -> parser.readTypes(sourceCodes))
                .orElseGet(() -> new TypeAliases(Collections.emptyList(), Collections.emptyList()));
    }
}
