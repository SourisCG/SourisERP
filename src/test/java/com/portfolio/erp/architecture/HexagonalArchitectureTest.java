package com.portfolio.erp.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the Dependency Rule of the hexagonal architecture:
 * inner layers know nothing about outer layers or frameworks.
 */
@AnalyzeClasses(packages = "com.portfolio.erp", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "org.hibernate..",
                    "com.portfolio.erp.application..",
                    "com.portfolio.erp.infrastructure..",
                    "com.portfolio.erp.config..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_only_depends_on_domain = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.portfolio.erp.infrastructure..",
                    "com.portfolio.erp.config..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_does_not_use_persistence = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_do_not_use_jpa_entities = noClasses()
            .that().resideInAPackage("..infrastructure.in.web..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.out.entity..")
            .allowEmptyShould(true);
}
