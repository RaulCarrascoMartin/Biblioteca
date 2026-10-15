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
 * <p>Módulos de negocio: {@code libro}, {@code usuario}, {@code prestamo} y {@code auth}. Cada uno es un
 * paquete de primer nivel con tres zonas: {@code domain} (entidades, servicios, puertos, DTO y excepciones:
 * su <b>API pública</b>) y los adaptadores {@code web}, {@code persistence}, {@code seed} y {@code security}
 * (su <b>interior</b>, privado para los demás módulos).
 * {@code shared} es el núcleo compartido y {@code config} la composición transversal de la aplicación.
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

    // ---- Encapsulación: el interior de un módulo no es accesible desde fuera ---------------------

    @ArchTest
    static final ArchRule el_interior_de_un_modulo_es_privado_para_los_demas =
            classes().that().resideInAnyPackage(
                            BASE + ".*.web..", BASE + ".*.persistence..", BASE + ".*.seed..", BASE + ".*.security..")
                    .should(sloSerAccedidasDesdeSuPropioModulo());

    private static ArchCondition<JavaClass> sloSerAccedidasDesdeSuPropioModulo() {
        return new ArchCondition<>("ser accedidas solo desde su propio módulo") {
            @Override
            public void check(JavaClass interna, ConditionEvents events) {
                String modulo = modulo(interna);
                interna.getDirectDependenciesToSelf().stream()
                        .filter(d -> !modulo.equals(modulo(d.getOriginClass())))
                        .filter(d -> modulo(d.getOriginClass()) != null)
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

    // ---- Arquitectura hexagonal dentro de cada módulo ----------------------------------------

    @ArchTest
    static final ArchRule el_nucleo_del_modulo_no_depende_de_sus_adaptadores =
            noClasses().that().resideInAnyPackage(
                            BASE + ".*.domain..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".*.web..", BASE + ".*.persistence..", BASE + ".*.seed..", BASE + ".*.security..");

    @ArchTest
    static final ArchRule la_persistencia_no_depende_de_la_web =
            noClasses().that().resideInAPackage(BASE + ".*.persistence..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".*.web..");

    @ArchTest
    static final ArchRule la_web_no_accede_a_la_persistencia =
            noClasses().that().resideInAPackage(BASE + ".*.web..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".*.persistence..");

    @ArchTest
    static final ArchRule los_modulos_solo_tienen_subpaquetes_conocidos =
            classes().that().resideInAnyPackage(BASE + ".libro..", BASE + ".usuario..", BASE + ".prestamo..", BASE + ".auth..")
                    .should().resideInAnyPackage(
                            BASE + ".*.domain..", BASE + ".*.web..", BASE + ".*.persistence..",
                            BASE + ".*.seed..", BASE + ".*.security..");

    // ---- Independencia de frameworks en el núcleo ----------------------------------------------

    @ArchTest
    static final ArchRule el_nucleo_no_usa_spring_security_ni_jwt =
            noClasses().that().resideInAnyPackage(
                            BASE + ".*.domain..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.security..", "io.jsonwebtoken..");

    @ArchTest
    static final ArchRule el_nucleo_no_usa_spring_data_ni_spring_mvc =
            noClasses().that().resideInAnyPackage(
                            BASE + ".*.domain..", BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.data..", "org.springframework.web..", "org.springframework.http..");

    @ArchTest
    static final ArchRule solo_la_web_y_config_conocen_http =
            noClasses().that().resideOutsideOfPackages(BASE + ".*.web..", BASE + ".config..", BASE + ".*.security..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..");

    // ---- Convenciones ----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule los_controladores_viven_en_web =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().resideInAPackage(BASE + ".*.web..");

    @ArchTest
    static final ArchRule las_entidades_jpa_viven_en_el_dominio =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().resideInAPackage(BASE + ".*.domain..");

    @ArchTest
    static final ArchRule los_repositorios_spring_data_viven_en_persistence =
            classes().that().areAssignableTo(org.springframework.data.repository.Repository.class)
                    .should().resideInAPackage(BASE + ".*.persistence..");

    @ArchTest
    static final ArchRule no_se_usa_inyeccion_por_campo =
            noClasses().should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.beans.factory.annotation.Autowired");
}
