package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.insight.Insights;
import org.dddjava.jig.domain.model.knowledge.insight.MethodInsight;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmells;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.StringComparingMethodList;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Service
public class JigService {

    private final TypesQueryService typesQueryService;
    private final InfrastructureQueryService infrastructureQueryService;
    private final UsecaseQueryService usecaseQueryService;

    public JigService(CoreDomainCondition coreDomainCondition, JigEventRepository jigEventRepository) {
        this.typesQueryService = new TypesQueryService(coreDomainCondition, jigEventRepository);
        this.infrastructureQueryService = new InfrastructureQueryService(jigEventRepository, this.typesQueryService);
        this.usecaseQueryService = new UsecaseQueryService(jigEventRepository, this.typesQueryService, this.infrastructureQueryService);
    }

    /**
     * 全JigTypeを取得する
     *
     * ここで取得されるJigTypesにはsourceで定義された全てのJigTypeが含まれる。
     * ライブラリのクラスなど、使用しているだけのものは入らない。
     */
    public JigTypes jigTypes(JigRepository jigRepository) {
        return typesQueryService.jigTypes(jigRepository);
    }

    /**
     * 用語集を取得する
     */
    public Glossary glossary(JigRepository jigRepository) {
        return typesQueryService.glossary(jigRepository);
    }

    /**
     * コアドメインのみのJigTypesを取得する
     *
     * コアドメインは実行時に指定するパターンなどによって識別する。
     */
    public JigTypes coreDomainJigTypes(JigRepository jigRepository) {
        return typesQueryService.coreDomainJigTypes(jigRepository);
    }

    public MethodSmells methodSmells(JigRepository jigRepository) {
        return typesQueryService.methodSmells(jigRepository);
    }

    public JigTypes categoryTypes(JigRepository jigRepository) {
        return typesQueryService.categoryTypes(jigRepository);
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return typesQueryService.serviceTypes(jigRepository);
    }

    public ServiceMethods serviceMethods(JigRepository jigRepository) {
        return usecaseQueryService.serviceMethods(jigRepository);
    }

    public InputAdapters inputAdapters(JigRepository jigRepository) {
        return usecaseQueryService.inputAdapters(jigRepository);
    }

    public ServiceAngles serviceAngles(JigRepository jigRepository) {
        return usecaseQueryService.serviceAngles(jigRepository);
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        return infrastructureQueryService.datasourceAngles(jigRepository);
    }

    public StringComparingMethodList stringComparing(JigRepository jigRepository) {
        return usecaseQueryService.stringComparing(jigRepository);
    }

    public CoreTypesAndRelations coreTypesAndRelations(JigRepository jigRepository) {
        return typesQueryService.coreTypesAndRelations(jigRepository);
    }

    public JigPackages packages(JigRepository jigRepository) {
        var jigTypes = jigTypes(jigRepository);
        var glossary = glossary(jigRepository);

        Map<PackageId, List<JigType>> packageAndJigTypes = jigTypes.orderedStream()
                .collect(groupingBy(JigType::packageId));

        List<Term> packageTerms = glossary.terms().stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .toList();

        List<JigPackage> jigPackages = Stream.concat(
                        packageAndJigTypes.keySet().stream(),
                        packageTerms.stream().map(Term::id).map(TermId::asText).map(PackageId::valueOf))
                .distinct()
                .map(packageId -> {
                    var packageTerm = glossary.termOf(packageId.asText(), TermKind.パッケージ);
                    return new JigPackage(packageId, packageTerm, packageAndJigTypes.getOrDefault(packageId, List.of()));
                })
                .toList();

        return new JigPackages(jigPackages);
    }

    public Insights insights(JigRepository repository) {
        return new Insights(
                jigTypes(repository).orderedStream()
                        .flatMap(JigType::allJigMethodStream)
                        .map(MethodInsight::new)
                        .toList(),
                glossary(repository)
        );
    }
}
