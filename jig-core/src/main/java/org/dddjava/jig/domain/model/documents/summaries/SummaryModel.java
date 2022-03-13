package org.dddjava.jig.domain.model.documents.summaries;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class SummaryModel {
    Map<PackageIdentifier, List<JigType>> map;
    // FIXME enumModelsの持ち方・・・
    EnumModels enumModels;

    SummaryModel(Map<PackageIdentifier, List<JigType>> map) {
        this(map, new EnumModels(List.of()));
    }

    SummaryModel(Map<PackageIdentifier, List<JigType>> map, EnumModels enumModels) {
        this.map = map;
        this.enumModels = enumModels;
    }

    public static SummaryModel from(BusinessRules businessRules) {
        Map<PackageIdentifier, List<JigType>> map = businessRules.list().stream()
                .map(BusinessRule::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map);
    }

    public static SummaryModel from(ServiceMethods serviceMethods) {
        Map<PackageIdentifier, List<JigType>> map = serviceMethods.listJigTypes().stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map);
    }

    public static SummaryModel from(CategoryTypes categoryTypes, EnumModels enumModels) {
        Map<PackageIdentifier, List<JigType>> map = categoryTypes.list().stream()
                .map(CategoryType::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map, enumModels);
    }

    public Map<PackageIdentifier, List<JigType>> map() {
        return map;
    }

    public boolean empty() {
        return map.isEmpty();
    }

    public EnumModels enumModels() {
        return enumModels;
    }
}
