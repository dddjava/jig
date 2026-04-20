package org.dddjava.jig.domain.model.information.inbound;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.SpringAnnotations;

record SchedulerEntrypointMapping(JigMethod jigMethod) implements EntrypointMapping {

    @Override
    public String fullPathText() {
        return jigMethod.declarationAnnotationStream()
                .filter(a -> a.id().equals(SpringAnnotations.SCHEDULED))
                .map(a -> a.elementTextOf("cron")
                        .map(v -> "cron: " + v)
                        .or(() -> a.elementTextOf("fixedRateString").map(v -> "rate: " + v))
                        .or(() -> a.elementTextOf("fixedRate").map(v -> "rate: " + v + "ms"))
                        .or(() -> a.elementTextOf("fixedDelayString").map(v -> "delay: " + v))
                        .or(() -> a.elementTextOf("fixedDelay").map(v -> "delay: " + v + "ms"))
                        .orElse("???"))
                .findAny()
                .orElse("???");
    }
}
