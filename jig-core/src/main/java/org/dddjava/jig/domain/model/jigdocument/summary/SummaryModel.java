package org.dddjava.jig.domain.model.jigdocument.summary;

import org.dddjava.jig.domain.model.models.applications.ServiceMethods;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class SummaryModel {
    Map<PackageIdentifier, List<JigType>> map;

    SummaryModel(Map<PackageIdentifier, List<JigType>> map) {
        this.map = map;
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

    public Map<PackageIdentifier, List<JigType>> map() {
        return map;
    }

    public boolean empty() {
        return map.isEmpty();
    }
}
