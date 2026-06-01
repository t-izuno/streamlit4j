package io.streamlit4j.server;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = {"io.streamlit4j.core", "io.streamlit4j.server"},
        importOptions = ImportOption.DoNotIncludeTests.class)
final class ServerArchitectureTest {

    private ServerArchitectureTest() {}

    @ArchTest
    static final ArchRule server_does_not_touch_core_runtime = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.server..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("io.streamlit4j.core.runtime..")
            .because("server is an adapter — it composes the app via core.bootstrap and uses ports / use cases only");

    @ArchTest
    static final ArchRule no_cycles_across_core_and_server =
            slices().matching("io.streamlit4j.(*)..").should().beFreeOfCycles();
}
