package io.streamlit4j.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "io.streamlit4j.core", importOptions = ImportOption.DoNotIncludeTests.class)
final class CoreArchitectureTest {

    private CoreArchitectureTest() {}

    @ArchTest
    static final ArchRule core_is_web_framework_agnostic = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.eclipse.jetty..",
                    "jakarta.servlet..",
                    "jakarta.websocket..",
                    "javax.servlet..",
                    "org.springframework..",
                    "io.netty..")
            .because("core must remain pure execution engine (design.md §5.1)");

    @ArchTest
    static final ArchRule stackwalker_is_encapsulated_in_widget_ids = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core..")
            .and()
            .haveSimpleNameNotEndingWith("WidgetIds")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.lang.StackWalker")
            .because("StackWalker is encapsulated in WidgetIds (design.md §2.3)");

    @ArchTest
    static final ArchRule threadlocal_is_encapsulated_in_render_context = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core..")
            .and()
            .haveSimpleNameNotEndingWith("RenderContext")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.lang.ThreadLocal")
            .because("ThreadLocal binding is encapsulated to allow future ScopedValue migration (design.md §2.2)");

    @ArchTest
    static final ArchRule virtual_thread_executor_only_in_script_runner = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core..")
            .and()
            .haveSimpleNameNotEndingWith("ScriptRunner")
            .should()
            .callMethod(java.util.concurrent.Executors.class, "newVirtualThreadPerTaskExecutor")
            .because("virtual thread executor is owned by ScriptRunner (design.md §2.1)");

    @ArchTest
    static final ArchRule domain_only_depends_on_protocol_at_most = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "io.streamlit4j.core.port..",
                    "io.streamlit4j.core.application..",
                    "io.streamlit4j.core.runtime..",
                    "io.streamlit4j.core.bootstrap..")
            .because("domain may use protocol DTOs but never depend on adapters (design.md §0)");

    @ArchTest
    static final ArchRule protocol_is_pure_data = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core.protocol..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "io.streamlit4j.core.domain..",
                    "io.streamlit4j.core.port..",
                    "io.streamlit4j.core.application..",
                    "io.streamlit4j.core.runtime..",
                    "io.streamlit4j.core.bootstrap..")
            .because("protocol DTOs must remain pure data with no engine dependencies (design.md §0)");

    @ArchTest
    static final ArchRule port_only_depends_on_domain_and_protocol = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core.port..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "io.streamlit4j.core.application..",
                    "io.streamlit4j.core.runtime..",
                    "io.streamlit4j.core.bootstrap..")
            .because("ports define boundaries — they must not depend on adapters or use cases (design.md §0)");

    @ArchTest
    static final ArchRule application_does_not_depend_on_runtime_or_bootstrap = noClasses()
            .that()
            .resideInAPackage("io.streamlit4j.core.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("io.streamlit4j.core.runtime..", "io.streamlit4j.core.bootstrap..")
            .because("use cases interact with the outside via ports only (design.md §0)");

    @ArchTest
    static final ArchRule no_package_cycles =
            slices().matching("io.streamlit4j.core.(*)..").should().beFreeOfCycles();
}
