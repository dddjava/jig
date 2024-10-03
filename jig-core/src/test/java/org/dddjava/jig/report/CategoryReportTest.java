package org.dddjava.jig.report;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReadService;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.infrastructure.view.report.business_rule.CategoryReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class CategoryReportTest {

    @Test
    void test(JigService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        CategoryDiagram categoryDiagram = businessRuleService.categories();

        assertThat(categoryDiagram.list().stream().map(CategoryReport::new))
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
