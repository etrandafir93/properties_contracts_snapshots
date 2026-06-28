package com.clearinghouse;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses
class PipesAndFilters_ArchTest {

    @ArchTest
    ArchRule filtersShouldNotDependOnOtherFilters = noClasses()
            .that().areAnnotatedWith(Filter.class)
            .should().dependOnClassesThat().areAnnotatedWith(Filter.class)
            .because("filters must communicate through pipes (Spring Cloud Stream bindings), not by direct dependency");

}
