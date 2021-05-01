package org.dddjava.jig.report;

import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.presentation.view.report.business_rule.CategoryReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class CategoryReportTest {

    @Test
    void test(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        Categories categories = businessRuleService.categories();

        assertThat(categories.list().stream().map(CategoryReport::new))
                .extracting(
                        categoryReport -> categoryReport.typeIdentifier(),
                        categoryReport -> categoryReport.constantsDeclarationsName(),
                        categoryReport -> categoryReport.fieldDeclarations(),
                        categoryReport -> categoryReport.userTypeIdentifiers().asSimpleText(),
                        categoryReport -> categoryReport.hasParameter(),
                        categoryReport -> categoryReport.hasBehaviour(),
                        categoryReport -> categoryReport.isPolymorphism()
                ).contains(
                tuple(
                        new TypeIdentifier("stub.domain.model.category.SimpleEnum"),
                        "[A, B, C, D]", "[]", "[RelationEnum]",
                        false, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.HasStaticFieldEnum"),
                        "[A, B]", "[]", "[]",
                        false, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.ParameterizedEnum"),
                        "[A, B]", "[String param]", "[RelationEnum]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.BehaviourEnum"),
                        "[A, B]", "[]", "[RelationEnum]",
                        false, true, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.PolymorphismEnum"),
                        "[A, B]", "[]", "[RelationEnum]",
                        false, false, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.RelationEnum"),
                        "[A, B, C]", "[RichEnum field]", "[]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.RichEnum"),
                        "[A, B]", "[String param]", "[RelationEnum]",
                        true, true, true
                )
        );
    }
}
