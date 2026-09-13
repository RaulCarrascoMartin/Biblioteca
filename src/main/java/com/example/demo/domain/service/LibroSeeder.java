package com.example.demo.domain.service;

import com.example.demo.domain.entity.Libro;
import com.example.demo.persistance.repository.LibroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LibroSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LibroSeeder.class);

    private final LibroRepository libroRepository;

    public LibroSeeder(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Override
    public void run(String... args) {
        int nuevos = 0;
        for (Libro libro : catalogo()) {
            if (libroRepository.findByIsbn(libro.getIsbn()).isEmpty()) {
                libro.setDisponible(true);
                libroRepository.save(libro);
                nuevos++;
            }
        }
        if (nuevos > 0) {
            log.info("LibroSeeder: {} volúmenes añadidos al catálogo", nuevos);
        }
    }

    private static List<Libro> catalogo() {
        return List.of(
                libro("Don Quijote de la Mancha", "Miguel de Cervantes", "9788423901500"),
                libro("La Regenta", "Leopoldo Alas (Clarín)", "9781721979912"),
                libro("Fortunata y Jacinta", "Benito Pérez Galdós", "9788475063904"),
                libro("Rimas y Leyendas", "Gustavo Adolfo Bécquer", "9788470022180"),
                libro("El árbol de la ciencia", "Pío Baroja", "9798764893556"),
                libro("Platero y yo", "Juan Ramón Jiménez", "9789875504363"),
                libro("La casa de Bernarda Alba", "Federico García Lorca", "9788420486765"),
                libro("Luces de bohemia", "Ramón del Valle-Inclán", "9788423931804"),
                libro("San Manuel Bueno, mártir", "Miguel de Unamuno", "9798729606016"),
                libro("La colmena", "Camilo José Cela", "9788484507949"),
                libro("La sombra del viento", "Carlos Ruiz Zafón", "9784087605082"),
                libro("Crónica de una muerte anunciada", "Gabriel García Márquez", "9788439703860"),
                libro("El amor en los tiempos del cólera", "Gabriel García Márquez", "9780307387264"),
                libro("La casa de los espíritus", "Isabel Allende", "9783804416635"),
                libro("Rayuela", "Julio Cortázar", "9788435004459"),
                libro("Ficciones", "Jorge Luis Borges", "9789754706574"),
                libro("El túnel", "Ernesto Sabato", "9789871144266"),
                libro("Pedro Páramo", "Juan Rulfo", "9798663628426"),
                libro("La ciudad y los perros", "Mario Vargas Llosa", "9789681903978"),
                libro("Veinte poemas de amor y una canción desesperada", "Pablo Neruda", "9788416259595"),
                libro("Orgullo y prejuicio", "Jane Austen", "9788415083696"),
                libro("Cumbres borrascosas", "Emily Brontë", "9798708166524"),
                libro("Moby Dick", "Herman Melville", "9781400092727"),
                libro("Crimen y castigo", "Fiódor Dostoyevski", "9788418008122"),
                libro("1984", "George Orwell", "9789899135024"),
                libro("El gran Gatsby", "Francis Scott Fitzgerald", "9788429761221"),
                libro("Matar a un ruiseñor", "Harper Lee", "9780060935467"),
                libro("El guardián entre el centeno", "J.D. Salinger", "9781613839560"),
                libro("Los miserables", "Victor Hugo", "9781115828161"),
                libro("Ana Karenina", "León Tolstói", "9780141199610"),
                libro("El viejo y el mar", "Ernest Hemingway", "9781541324565"),
                libro("Fahrenheit 451", "Ray Bradbury", "9780745171067")
        );
    }

    private static Libro libro(String titulo, String autor, String isbn) {
        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setIsbn(isbn);
        return libro;
    }
}