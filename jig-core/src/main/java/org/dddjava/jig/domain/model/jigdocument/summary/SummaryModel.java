package org.dddjava.jig.domain.model.jigdocument.summary;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;

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

    public static SummaryModel from(Map<PackageIdentifier, List<JigType>> serviceMap) {
        return new SummaryModel(serviceMap);
    }

    public Map<PackageIdentifier, List<JigType>> map() {
        return map;
    }

    public boolean empty() {
        return map.isEmpty();
    }
}
