package com.example.demo;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Reglas ArchUnit para lo que Spring Modulith no cubre (el límite entre módulos lo verifica
 * {@code ApplicationModulesTest}). Esquema por módulo ({@code catalog}, {@code users}, {@code loans},
 * {@code auth}): API pública en la raíz y {@code dto}; privado en {@code internal} con
 * {@code application, controller (dto, handler, mapper), usecase, domain (model, dto, mapper, service,
 * repository, exception), persistence}.
 *
 * <p>Flujos: {@code Controller -> UseCase -> Service -> Repository <- RepositoryImpl} (HTTP) y
 * {@code XModule -> XModuleImpl -> Service} (otros módulos). El {@code UseCase} no depende de nada de
 * {@code controller}. {@code persistence}: {@code dao (entity, impl.jpa)} y {@code repository (mapper, impl)}.
 */
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.example.demo";
    private static final String INTERNAL = BASE + ".*.internal..";
    private static final String CONTROLLER = BASE + ".*.internal.controller..";
    private static final String USECASE = BASE + ".*.internal.usecase..";
    private static final String DOMAIN = BASE + ".*.internal.domain..";
    private static final String SERVICE = BASE + ".*.internal.domain.service..";
    private static final String PERSISTENCE = BASE + ".*.internal.persistence..";
    private static final String PERSISTENCE_DAO_JPA = BASE + ".*.internal.persistence.dao.impl.jpa..";
    private static final String APPLICATION = BASE + ".*.internal.application..";
    private static final String[] BUSINESS_MODULES = {"catalog", "users", "loans", "auth"};

    // ---- Módulos y encapsulación ---------------------------------------------------------------

    @ArchTest
    static final ArchRule modules_are_free_of_cycles =
            slices().matching(BASE + ".(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule internal_is_private_to_its_own_module =
            classes().that().resideInAPackage(INTERNAL).should(beAccessedOnlyFromTheirOwnModule());

    @ArchTest
    static final ArchRule public_api_does_not_depend_on_internal_except_module_impl =
            noClasses().that().resideInAnyPackage(BASE + ".*.dto..", BASE + ".*.exception..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAPackage(INTERNAL);

    @ArchTest
    static final ArchRule no_module_depends_on_config =
            noClasses().that().resideOutsideOfPackage(BASE + ".config..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".config..");

    @ArchTest
    static final ArchRule modules_only_have_known_subpackages =
            classes().that().resideInAnyPackage(moduleTrees())
                    .should().resideInAnyPackage(
                            BASE + ".catalog", BASE + ".users", BASE + ".loans", BASE + ".auth",
                            BASE + ".*.dto", BASE + ".*.exception",
                            BASE + ".*.internal.application",
                            BASE + ".*.internal.controller", BASE + ".*.internal.controller.dto",
                            BASE + ".*.internal.controller.handler", BASE + ".*.internal.controller.mapper",
                            BASE + ".*.internal.usecase",
                            BASE + ".*.internal.domain.model", BASE + ".*.internal.domain.dto",
                            BASE + ".*.internal.domain.mapper", BASE + ".*.internal.domain.service",
                            BASE + ".*.internal.domain.repository", BASE + ".*.internal.domain.exception",
                            BASE + ".*.internal.persistence.dao",
                            BASE + ".*.internal.persistence.dao.entity",
                            BASE + ".*.internal.persistence.dao.impl.jpa",
                            BASE + ".*.internal.persistence.repository",
                            BASE + ".*.internal.persistence.repository.mapper",
                            BASE + ".*.internal.persistence.repository.impl",
                            BASE + ".*.internal.persistence.projection",
                            // Excepción documentada: vetos síncronos entre módulos (@NamedInterface("spi"))
                            BASE + ".*.spi");

    // ---- Capas dentro del módulo ---------------------------------------------------------------

    @ArchTest
    static final ArchRule controllers_only_use_usecases_not_services_or_persistence =
            noClasses().that().resideInAPackage(CONTROLLER)
                    .should().dependOnClassesThat().resideInAnyPackage(SERVICE, PERSISTENCE, DOMAIN + "repository..");

    @ArchTest
    static final ArchRule usecases_do_not_depend_on_each_other =
            noClasses().that().haveSimpleNameEndingWith("UseCase")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule usecases_do_not_use_controller_or_persistence =
            noClasses().that().resideInAPackage(USECASE)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            CONTROLLER, PERSISTENCE, DOMAIN + "repository..");

    @ArchTest
    static final ArchRule module_api_and_impl_do_not_depend_on_usecases_or_controllers =
            noClasses().that().haveSimpleNameEndingWith("Module").or().haveSimpleNameEndingWith("ModuleImpl")
                    .should().dependOnClassesThat().resideInAnyPackage(USECASE, CONTROLLER);

    @ArchTest
    static final ArchRule application_layer_does_not_use_web_or_persistence =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAnyPackage(CONTROLLER, USECASE, PERSISTENCE);

    @ArchTest
    static final ArchRule domain_does_not_depend_on_other_layers =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            PERSISTENCE, CONTROLLER, USECASE, APPLICATION);

    @ArchTest
    static final ArchRule domain_is_free_of_web_validation_and_persistence_technology =
            // Permitido en el dominio: org.springframework.stereotype/transaction (@Service, @Transactional)
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web..", "org.springframework.http..", "jakarta.validation..",
                            "jakarta.persistence..", "org.springframework.data..", "org.springframework.security..",
                            "jakarta.servlet..", "io.jsonwebtoken..");

    @ArchTest
    static final ArchRule persistence_does_not_depend_on_web_or_usecases =
            noClasses().that().resideInAPackage(PERSISTENCE)
                    .should().dependOnClassesThat().resideInAnyPackage(CONTROLLER, USECASE, APPLICATION);

    @ArchTest
    static final ArchRule domain_repositories_are_interfaces =
            classes().that().resideInAPackage(DOMAIN + "repository..").should().beInterfaces();

    @ArchTest
    static final ArchRule domain_services_are_interfaces_or_impl =
            classes().that().resideInAPackage(SERVICE).and().haveSimpleNameNotEndingWith("Impl")
                    .and().areNotAnonymousClasses().and().areNotNestedClasses()
                    .and(com.tngtech.archunit.base.DescribedPredicate.describe("not synthetic",
                            (JavaClass c) -> !c.getModifiers().contains(JavaModifier.SYNTHETIC)))
                    .should().beInterfaces();

    // ---- Ubicación por sufijo --------------------------------------------------------------------

    @ArchTest
    static final ArchRule services_live_in_domain_service =
            classes().that().haveSimpleNameEndingWith("Service").or().haveSimpleNameEndingWith("ServiceImpl")
                    .and().resideInAPackage(BASE + ".*.internal..")
                    .should().resideInAPackage(SERVICE);

    @ArchTest
    static final ArchRule repositories_live_in_domain_repository =
            classes().that().haveSimpleNameEndingWith("Repository").and().resideInAPackage(BASE + ".*.internal..")
                    .and().haveSimpleNameNotEndingWith("SpringDataRepository")
                    .should().resideInAPackage(DOMAIN + "repository..");

    @ArchTest
    static final ArchRule spring_data_repositories_live_in_dao_impl_jpa =
            classes().that().haveSimpleNameEndingWith("SpringDataRepository")
                    .should().resideInAPackage(PERSISTENCE_DAO_JPA);

    @ArchTest
    static final ArchRule dtos_live_in_dto_packages =
            classes().that().haveSimpleNameEndingWith("Dto").and().areNotAnonymousClasses()
                    .and().resideInAPackage(BASE + "..")
                    .should().resideInAnyPackage(DOMAIN + "dto..", BASE + ".*.dto..");

    @ArchTest
    static final ArchRule web_dtos_live_in_controller_dto =
            classes().that().haveSimpleNameEndingWith("Request").or().haveSimpleNameEndingWith("Response")
                    .and().resideInAPackage(BASE + "..")
                    .should().resideInAPackage(BASE + ".*.internal.controller.dto..");

    @ArchTest
    static final ArchRule entities_live_in_dao_entity =
            classes().that().haveSimpleNameEndingWith("Entity").and().resideInAPackage(BASE + "..")
                    .should().resideInAPackage(BASE + ".*.internal.persistence.dao.entity..");

    @ArchTest
    static final ArchRule persistence_mappers_live_in_repository_mapper =
            classes().that().haveSimpleNameEndingWith("PersistenceMapper").and().resideInAPackage(BASE + "..")
                    .should().resideInAPackage(BASE + ".*.internal.persistence.repository.mapper..");

    @ArchTest
    static final ArchRule repository_impls_live_in_persistence =
            classes().that().haveSimpleNameEndingWith("RepositoryImpl")
                    .should().resideInAPackage(PERSISTENCE)
                    .andShould().implement(com.tngtech.archunit.base.DescribedPredicate.describe(
                            "a domain repository interface",
                            (JavaClass i) -> i.getPackageName().contains(".internal.domain.repository")));

    @ArchTest
    static final ArchRule usecases_live_in_usecase_package =
            classes().that().haveSimpleNameEndingWith("UseCase").should().resideInAPackage(USECASE);

    @ArchTest
    static final ArchRule controllers_live_in_controller_package =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().resideInAPackage(CONTROLLER)
                    .andShould().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule module_impls_live_in_application =
            classes().that().haveSimpleNameEndingWith("ModuleImpl").should().resideInAPackage(APPLICATION);

    @ArchTest
    static final ArchRule mappers_live_in_mapper_packages_and_use_mapstruct =
            classes().that().haveSimpleNameEndingWith("Mapper").and().resideInAPackage(BASE + ".*.internal..")
                    .and().haveSimpleNameNotEndingWith("PersistenceMapper")
                    .should().resideInAnyPackage(
                            BASE + ".*.internal.controller.mapper..", BASE + ".*.internal.domain.mapper..")
                    .andShould().beAnnotatedWith(org.mapstruct.Mapper.class);

    @ArchTest
    static final ArchRule exception_handlers_live_in_controller_handler =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestControllerAdvice.class)
                    .and().resideInAPackage(BASE + ".*.internal..")
                    .should().resideInAPackage(BASE + ".*.internal.controller.handler..");

    @ArchTest
    static final ArchRule jpa_entities_live_in_persistence =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class).should().resideInAPackage(PERSISTENCE);

    @ArchTest
    static final ArchRule spring_data_repositories_live_in_persistence =
            classes().that().areAssignableTo(org.springframework.data.repository.Repository.class)
                    .should().resideInAPackage(PERSISTENCE);

    @ArchTest
    static final ArchRule public_dtos_do_not_expose_domain_models =
            // Nombres libres en <modulo>.dto (BookSummary, UserSummary, NewUser...)
            noClasses().that().resideInAnyPackage(BASE + ".*.dto..")
                    .should().dependOnClassesThat().resideInAnyPackage(DOMAIN);

    @ArchTest
    static final ArchRule domain_and_persistence_do_not_use_web_dto_naming_mixups =
            // Los DTO del dominio terminan en Dto
            classes().that().resideInAPackage(DOMAIN + "dto..").should().haveSimpleNameEndingWith("Dto");

    // ---- shared y config -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule shared_is_free_of_spring_and_jakarta =
            // org.springframework.modulith: solo la anotación de package-info
            noClasses().that().resideInAPackage(BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.stereotype..", "org.springframework.beans..",
                            "org.springframework.context..", "org.springframework.web..",
                            "org.springframework.http..", "org.springframework.security..",
                            "org.springframework.data..", "org.springframework.core..",
                            "jakarta..", "lombok..");

    @ArchTest
    static final ArchRule shared_does_not_depend_on_modules =
            noClasses().that().resideInAPackage(BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".catalog..", BASE + ".users..", BASE + ".loans..", BASE + ".auth..", BASE + ".config..");

    // ---- Autorización, eventos, inyección --------------------------------------------------------

    @ArchTest
    static final ArchRule every_public_usecase_method_takes_requester_first =
            classes().that().haveSimpleNameEndingWith("UseCase")
                    .should(haveRequesterAsFirstParameterInPublicMethods());

    @ArchTest
    static final ArchRule method_security_and_security_context_only_in_config =
            noClasses().that().resideOutsideOfPackage(BASE + ".config..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.security.access.prepost.PreAuthorize")
                    .orShould().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.security.access.annotation.Secured")
                    .orShould().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.security.core.context.SecurityContextHolder");

    @ArchTest
    static final ArchRule controllers_do_not_import_spring_security =
            noClasses().that().resideInAPackage(CONTROLLER)
                    .should().dependOnClassesThat().resideInAPackage("org.springframework.security..");

    @ArchTest
    static final ArchRule no_application_events =
            noClasses().should().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.context.ApplicationEventPublisher")
                    .orShould().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.context.event.EventListener")
                    .orShould().dependOnClassesThat().haveFullyQualifiedName(
                            "org.springframework.modulith.events.ApplicationModuleListener");

    @ArchTest
    static final ArchRule no_field_injection =
            noClasses().should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule servlet_api_only_in_controller_and_config =
            noClasses().that().resideOutsideOfPackages(CONTROLLER, BASE + ".config..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..");

    // ---- Condiciones propias -----------------------------------------------------------------------

    private static String[] moduleTrees() {
        String[] trees = new String[BUSINESS_MODULES.length];
        for (int i = 0; i < trees.length; i++) {
            trees[i] = BASE + "." + BUSINESS_MODULES[i] + "..";
        }
        return trees;
    }

    private static ArchCondition<JavaClass> beAccessedOnlyFromTheirOwnModule() {
        return new ArchCondition<>("be accessed only from their own module") {
            @Override
            public void check(JavaClass internal, ConditionEvents events) {
                String module = module(internal);
                internal.getDirectDependenciesToSelf().stream()
                        .filter(d -> module(d.getOriginClass()) != null)
                        .filter(d -> !module.equals(module(d.getOriginClass())))
                        .forEach(d -> events.add(SimpleConditionEvent.violated(d, d.getDescription())));
            }
        };
    }

    private static String module(JavaClass clazz) {
        String pkg = clazz.getPackageName();
        if (!pkg.startsWith(BASE + ".")) {
            return null;
        }
        String rest = pkg.substring(BASE.length() + 1);
        int dot = rest.indexOf('.');
        return dot < 0 ? rest : rest.substring(0, dot);
    }

    /** Todo método público de un UseCase recibe Requester primero, salvo register, login y refresh de AuthUseCase. */
    private static ArchCondition<JavaClass> haveRequesterAsFirstParameterInPublicMethods() {
        return new ArchCondition<>("have Requester as the first parameter in every public method") {
            @Override
            public void check(JavaClass useCase, ConditionEvents events) {
                for (JavaMethod method : useCase.getMethods()) {
                    if (!method.getModifiers().contains(JavaModifier.PUBLIC) || method.getModifiers().contains(JavaModifier.SYNTHETIC)) {
                        continue;
                    }
                    boolean publicFlow = useCase.getSimpleName().equals("AuthUseCase")
                            && (method.getName().equals("register") || method.getName().equals("login")
                            || method.getName().equals("refresh"));
                    if (publicFlow) {
                        continue;
                    }
                    var params = method.getRawParameterTypes();
                    boolean ok = !params.isEmpty()
                            && params.get(0).getFullName().equals("com.example.demo.shared.Requester");
                    if (!ok) {
                        events.add(SimpleConditionEvent.violated(method,
                                method.getFullName() + " no recibe Requester como primer parámetro"));
                    }
                }
            }
        };
    }
}
