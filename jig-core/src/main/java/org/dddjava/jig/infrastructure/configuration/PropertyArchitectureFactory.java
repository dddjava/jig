package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.data.Architecture;
import org.dddjava.jig.domain.model.data.classes.type.JigType;

import java.util.regex.Pattern;

public class PropertyArchitectureFactory {

    JigProperties jigProperties;

    public PropertyArchitectureFactory(JigProperties jigProperties) {
        this.jigProperties = jigProperties;
    }

    public Architecture architecture() {
        Pattern compilerGeneratedClassPattern = Pattern.compile(".+\\$\\d+");
        Pattern businessRulePattern = Pattern.compile(jigProperties.getDomainPattern());

        return new Architecture() {

            @Override
            public boolean isCoreDomain(JigType jigType) {
                String fqn = jigType.identifier().fullQualifiedName();
                if (fqn.endsWith(".package-info")) return false;
                return businessRulePattern.matcher(fqn).matches()
                        && !compilerGeneratedClassPattern.matcher(fqn).matches();
            }
        };
    }
}
