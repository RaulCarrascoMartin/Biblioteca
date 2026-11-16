package com.example.demo;

import com.tngtech.archunit.core.domain.JavaClass;
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
 * Reglas de arquitectura del monolito modular.
 *
 * <p>Módulos de negocio: {@code libro}, {@code usuario}, {@code prestamo} y {@code auth}. Cada módulo
 * tiene una <b>API pública</b> (la interfaz {@code XModule}, {@code dto}, {@code exception} y los SPI en la
 * raíz) y una <b>implementación privada</b> en {@code internal}, organizada en capas:
 * {@code application}, {@code web}, {@code domain}, {@code persistence}, {@code seed} y {@code security}.
 *
 * <p>El dominio es el centro: no depende de nada de infraestructura (JPA, Spring Data, web, seguridad);
 * {@code persistence} implementa los puertos que declara el dominio (inversión de dependencias).
 * Los módulos solo se comunican por la API pública y DTO, nunca con modelos ni entidades internas.
 * {@code shared} es el núcleo compartido y {@code config} la composición transversal.
 */
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.example.demo";

    // ---- Estructura modular -------------------------------------------------------------------

    @ArchTest
    static final ArchRule los_modulos_no_tienen_dependencias_ciclicas =
            slices().matching(BASE + ".(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule shared_no_depende_de_ningun_modulo =
            noClasses().that().resideInAPackage(BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".libro..", BASE + ".usuario..", BASE + ".prestamo..",
                            BASE + ".auth..", BASE + ".config..");

    @ArchTest
    static final ArchRule libro_no_depende_de_otros_modulos =
            noClasses().that().resideInAPackage(BASE + ".libro..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".usuario..", BASE + ".prestamo..", BASE + ".auth..", BASE + ".config..");

    @ArchTest
    static final ArchRule usuario_no_depende_de_otros_modulos =
            noClasses().that().resideInAPackage(BASE + ".usuario..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".libro..", BASE + ".prestamo..", BASE + ".auth..", BASE + ".config..");

    @ArchTest
    static final ArchRule prestamo_solo_depende_de_libro_y_usuario =
            noClasses().that().resideInAPackage(BASE + ".prestamo..")
                    .should().dependOnClassesThat().resideInAnyPackage(BASE + ".auth..", BASE + ".config..");

    @ArchTest
    static final ArchRule auth_solo_depende_de_usuario_y_shared =
            noClasses().that().resideInAPackage(BASE + ".auth..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".libro..", BASE + ".prestamo..", BASE + ".config..");

    @ArchTest
    static final ArchRule nadie_depende_de_config =
            noClasses().that().resideOutsideOfPackage(BASE + ".config..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".config..");

    // ---- Encapsulación: internal es privado; los módulos se hablan por API pública y DTO ----------

    @ArchTest
    static final ArchRule internal_es_privado_para_los_demas_modulos =
            classes().that().resideInAPackage(BASE + ".*.internal..")
                    .should(serAccedidasSoloDesdeSuPropioModulo());

    private static ArchCondition<JavaClass> serAccedidasSoloDesdeSuPropioModulo() {
        return new ArchCondition<>("ser accedidas solo desde su propio módulo") {
            @Override
            public void check(JavaClass interna, ConditionEvents events) {
                String modulo = modulo(interna);
                interna.getDirectDependenciesToSelf().stream()
                        .filter(d -> modulo(d.getOriginClass()) != null)
                        .filter(d -> !modulo.equals(modulo(d.getOriginClass())))
                        .forEach(d -> events.add(SimpleConditionEvent.violated(d, d.getDescription())));
            }
        };
    }

    private static String modulo(JavaClass clazz) {
        String pkg = clazz.getPackageName();
        if (!pkg.startsWith(BASE + ".")) {
            return null;
        }
        String resto = pkg.substring(BASE.length() + 1);
        int punto = resto.indexOf('.');
        return punto < 0 ? resto : resto.substring(0, punto);
    }

    @ArchTest
    static final ArchRule la_api_publica_no_depende_de_su_interior =
            noClasses().that().resideInAnyPackage(BASE + ".*.dto..", BASE + ".*.exception..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".*.internal..");

    @ArchTest
    static final ArchRule los_modulos_solo_tienen_subpaquetes_conocidos =
            classes().that().resideInAnyPackage(BASE + ".libro..", BASE + ".usuario..", BASE + ".prestamo..", BASE + ".auth..")
                    .should().resideInAnyPackage(
                            BASE + ".libro", BASE + ".usuario", BASE + ".prestamo", BASE + ".auth",
                            BASE + ".*.dto..", BASE + ".*.exception..",
                            BASE + ".*.internal.application..", BASE + ".*.internal.web..",
                            BASE + ".*.internal.domain..", BASE + ".*.internal.persistence..",
                            BASE + ".*.internal.seed..", BASE + ".*.internal.security..");

    // ---- Dominio: inversión de dependencias, no depende de nada de infraestructura -----------------

    @ArchTest
    static final ArchRule el_dominio_solo_depende_de_si_mismo_y_de_contratos_publicos =
            classes().that().resideInAPackage(BASE + ".*.internal.domain..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "java..", "lombok..", "org.mapstruct..", "org.slf4j..",
                            "org.springframework.stereotype..", "org.springframework.transaction..",
                            BASE + ".shared..",
                            BASE + ".*.internal.domain..",
                            BASE + ".*.dto..", BASE + ".*.exception..",
                            BASE + ".libro", BASE + ".usuario", BASE + ".prestamo", BASE + ".auth");

    @ArchTest
    static final ArchRule persistence_depende_del_dominio_nunca_al_reves =
            noClasses().that().resideInAPackage(BASE + ".*.internal.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".*.internal.persistence..", BASE + ".*.internal.web..",
                            BASE + ".*.internal.application..", BASE + ".*.internal.seed..",
                            BASE + ".*.internal.security..");

    @ArchTest
    static final ArchRule la_persistencia_no_depende_de_la_web =
            noClasses().that().resideInAPackage(BASE + ".*.internal.persistence..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".*.internal.web..");

    @ArchTest
    static final ArchRule la_web_no_accede_a_la_persistencia =
            noClasses().that().resideInAPackage(BASE + ".*.internal.web..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".*.internal.persistence..");

    @ArchTest
    static final ArchRule la_api_publica_no_usa_la_capa_web =
            noClasses().that().resideInAPackage(BASE + ".*.internal.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".*.internal.web..", BASE + ".*.internal.persistence..");

    // ---- Independencia de frameworks en el núcleo ----------------------------------------------

    @ArchTest
    static final ArchRule el_nucleo_no_usa_spring_security_ni_jwt =
            noClasses().that().resideInAnyPackage(
                            BASE + ".*.internal.domain..", BASE + ".*.dto..", BASE + ".*.exception..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.security..", "io.jsonwebtoken..");

    @ArchTest
    static final ArchRule el_nucleo_no_usa_jpa_spring_data_ni_spring_mvc =
            noClasses().that().resideInAnyPackage(
                            BASE + ".*.internal.domain..", BASE + ".*.dto..", BASE + ".*.exception..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data..",
                            "org.springframework.web..", "org.springframework.http..");

    @ArchTest
    static final ArchRule solo_la_web_y_config_conocen_http =
            noClasses().that().resideOutsideOfPackages(
                            BASE + ".*.internal.web..", BASE + ".config..", BASE + ".*.internal.security..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..");

    // ---- Convenciones ----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule los_controladores_viven_en_web =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().resideInAPackage(BASE + ".*.internal.web..");

    @ArchTest
    static final ArchRule las_entidades_jpa_viven_en_persistence =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().resideInAPackage(BASE + ".*.internal.persistence..");

    @ArchTest
    static final ArchRule los_repositorios_spring_data_viven_en_persistence =
            classes().that().areAssignableTo(org.springframework.data.repository.Repository.class)
                    .should().resideInAPackage(BASE + ".*.internal.persistence..");

    @ArchTest
    static final ArchRule los_mappers_son_mapstruct =
            classes().that().haveSimpleNameEndingWith("Mapper")
                    .should().beAnnotatedWith(org.mapstruct.Mapper.class);

    @ArchTest
    static final ArchRule no_se_usa_inyeccion_por_campo =
            noClasses().should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.beans.factory.annotation.Autowired");
}
