package com.example.demo.libro.internal.seed;

import com.example.demo.libro.dto.Genero;
import com.example.demo.libro.internal.domain.Libro;
import com.example.demo.libro.internal.domain.LibroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "app", name = "seed-books", havingValue = "true")
public class LibroSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LibroSeeder.class);

    private final LibroRepository libroRepository;

    public LibroSeeder(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Override
    public void run(String... args) {
        int nuevos = 0;
        int actualizados = 0;
        for (Libro seed : catalogo()) {
            Optional<Libro> existente = libroRepository.findByIsbn(seed.getIsbn());
            if (existente.isEmpty()) {
                seed.setDisponible(true);
                libroRepository.save(seed);
                nuevos++;
            } else {
                Libro libro = existente.get();
                if (libro.getGenero() != seed.getGenero()) {
                    libro.setGenero(seed.getGenero());
                    libroRepository.save(libro);
                    actualizados++;
                }
            }
        }
        if (nuevos > 0) {
            log.info("LibroSeeder: {} volúmenes añadidos al catálogo", nuevos);
        }
        if (actualizados > 0) {
            log.info("LibroSeeder: géneros asignados a {} volúmenes existentes", actualizados);
        }
    }

    private static List<Libro> catalogo() {
        return List.of(
                libro("Don Quijote de la Mancha", "Miguel de Cervantes", "9788423901500", Genero.CLASICA),
                libro("La Regenta", "Leopoldo Alas (Clarín)", "9781721979912", Genero.CLASICA),
                libro("Fortunata y Jacinta", "Benito Pérez Galdós", "9788475063904", Genero.CLASICA),
                libro("Rimas y Leyendas", "Gustavo Adolfo Bécquer", "9788470022180", Genero.POESIA),
                libro("El árbol de la ciencia", "Pío Baroja", "9798764893556", Genero.CLASICA),
                libro("Platero y yo", "Juan Ramón Jiménez", "9789875504363", Genero.POESIA),
                libro("La casa de Bernarda Alba", "Federico García Lorca", "9788420486765", Genero.CLASICA),
                libro("Luces de bohemia", "Ramón del Valle-Inclán", "9788423931804", Genero.CLASICA),
                libro("San Manuel Bueno, mártir", "Miguel de Unamuno", "9798729606016", Genero.CLASICA),
                libro("La colmena", "Camilo José Cela", "9788484507949", Genero.CLASICA),
                libro("La sombra del viento", "Carlos Ruiz Zafón", "9784087605082", Genero.MISTERIO),
                libro("Crónica de una muerte anunciada", "Gabriel García Márquez", "9788439703860", Genero.MISTERIO),
                libro("El amor en los tiempos del cólera", "Gabriel García Márquez", "9780307387264", Genero.ROMANTICA),
                libro("La casa de los espíritus", "Isabel Allende", "9783804416635", Genero.FANTASIA),
                libro("Rayuela", "Julio Cortázar", "9788435004459", Genero.CLASICA),
                libro("Ficciones", "Jorge Luis Borges", "9789754706574", Genero.FANTASIA),
                libro("El túnel", "Ernesto Sabato", "9789871144266", Genero.MISTERIO),
                libro("Pedro Páramo", "Juan Rulfo", "9798663628426", Genero.MISTERIO),
                libro("La ciudad y los perros", "Mario Vargas Llosa", "9789681903978", Genero.CLASICA),
                libro("Veinte poemas de amor y una canción desesperada", "Pablo Neruda", "9788416259595", Genero.POESIA),
                libro("Orgullo y prejuicio", "Jane Austen", "9788415083696", Genero.ROMANTICA),
                libro("Cumbres borrascosas", "Emily Brontë", "9798708166524", Genero.ROMANTICA),
                libro("Moby Dick", "Herman Melville", "9781400092727", Genero.AVENTURAS),
                libro("Crimen y castigo", "Fiódor Dostoyevski", "9788418008122", Genero.CLASICA),
                libro("1984", "George Orwell", "9789899135024", Genero.CIENCIA_FICCION),
                libro("El gran Gatsby", "Francis Scott Fitzgerald", "9788429761221", Genero.CLASICA),
                libro("Matar a un ruiseñor", "Harper Lee", "9780060935467", Genero.CLASICA),
                libro("El guardián entre el centeno", "J.D. Salinger", "9781613839560", Genero.CLASICA),
                libro("Los miserables", "Victor Hugo", "9781115828161", Genero.HISTORICA),
                libro("Ana Karenina", "León Tolstói", "9780141199610", Genero.CLASICA),
                libro("El viejo y el mar", "Ernest Hemingway", "9781541324565", Genero.AVENTURAS),
                libro("Fahrenheit 451", "Ray Bradbury", "9780745171067", Genero.CIENCIA_FICCION)
        );
    }

    private static Libro libro(String titulo, String autor, String isbn, Genero genero) {
        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setIsbn(isbn);
        libro.setGenero(genero);
        return libro;
    }
}