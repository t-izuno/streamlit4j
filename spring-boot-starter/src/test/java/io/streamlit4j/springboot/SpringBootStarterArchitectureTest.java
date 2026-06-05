package io.streamlit4j.springboot;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = {"io.streamlit4j.core", "io.streamlit4j.springboot"},
        importOptions = ImportOption.DoNotIncludeTests.class)
final class SpringBootStarterArchitectureTest {

    private SpringBootStarterArchitectureTest() {}

    @ArchTest
    static final ArchRule starter_does_not_touch_core_runtime = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.springboot..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("io.streamlit4j.core.runtime..")
            .because("starter is an adapter — it composes the app via core.bootstrap and uses ports / use cases only");

    @ArchTest
    static final ArchRule starter_does_not_depend_on_jetty = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.springboot..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.eclipse.jetty..", "io.netty..")
            .because(
                    "starter targets Spring's servlet/WebSocket stack only — embedded servers belong in other modules");

    @ArchTest
    static final ArchRule no_cycles_across_core_and_starter =
            slices().matching("io.streamlit4j.(*)..").should().beFreeOfCycles();
}
