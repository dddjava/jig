package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.Annotation;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    ALL,
    OTHER;

    static HttpMethod from(Annotation annotation) {
        String annotationTypeName = annotation.identifier().fullQualifiedName();

        switch (annotationTypeName) {
            case "org.springframework.web.bind.annotation.GetMapping":
                return GET;
            case "org.springframework.web.bind.annotation.PostMapping":
                return POST;
            case "org.springframework.web.bind.annotation.PutMapping":
                return PUT;
            case "org.springframework.web.bind.annotation.DeleteMapping":
                return DELETE;
            case "org.springframework.web.bind.annotation.RequestMapping":
                // org.springframework.web.bind.annotation.RequestMethod が入る
                String requestMethodEnum = annotation.descriptionTextOf("method");

                if (requestMethodEnum == null) return ALL;

                for (HttpMethod httpMethod : values()) {
                    if (requestMethodEnum.endsWith(httpMethod.name())) {
                        return httpMethod;
                    }
                }

                // TODO 複数指定をどうするか
        }
        return OTHER;
    }
}
