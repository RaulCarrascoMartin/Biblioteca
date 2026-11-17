package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.dto.SeedBooksDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/** Carga el catálogo inicial. Las reglas (ISBN duplicado, disponibilidad) las aplica el servicio. */
@Component
@ConditionalOnProperty(prefix = "app", name = "seed-books", havingValue = "true")
public class BookSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BookSeeder.class);

    private final BookService bookService;

    public BookSeeder(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
        SeedBooksDto result = bookService.seed(catalog());
        if (result.created() > 0) {
            log.info("BookSeeder: {} volúmenes añadidos al catálogo", result.created());
        }
        if (result.updated() > 0) {
            log.info("BookSeeder: géneros asignados a {} volúmenes existentes", result.updated());
        }
    }

    static List<SaveBookDto> catalog() {
        return List.of(
                book("Don Quijote de la Mancha", "Miguel de Cervantes", "9788423901500", Genre.CLASICA),
                book("La Regenta", "Leopoldo Alas (Clarín)", "9781721979912", Genre.CLASICA),
                book("Fortunata y Jacinta", "Benito Pérez Galdós", "9788475063904", Genre.CLASICA),
                book("Rimas y Leyendas", "Gustavo Adolfo Bécquer", "9788470022180", Genre.POESIA),
                book("El árbol de la ciencia", "Pío Baroja", "9798764893556", Genre.CLASICA),
                book("Platero y yo", "Juan Ramón Jiménez", "9789875504363", Genre.POESIA),
                book("La casa de Bernarda Alba", "Federico García Lorca", "9788420486765", Genre.CLASICA),
                book("Luces de bohemia", "Ramón del Valle-Inclán", "9788423931804", Genre.CLASICA),
                book("San Manuel Bueno, mártir", "Miguel de Unamuno", "9798729606016", Genre.CLASICA),
                book("La colmena", "Camilo José Cela", "9788484507949", Genre.CLASICA),
                book("La sombra del viento", "Carlos Ruiz Zafón", "9784087605082", Genre.MISTERIO),
                book("Crónica de una muerte anunciada", "Gabriel García Márquez", "9788439703860", Genre.MISTERIO),
                book("El amor en los tiempos del cólera", "Gabriel García Márquez", "9780307387264", Genre.ROMANTICA),
                book("La casa de los espíritus", "Isabel Allende", "9783804416635", Genre.FANTASIA),
                book("Rayuela", "Julio Cortázar", "9788435004459", Genre.CLASICA),
                book("Ficciones", "Jorge Luis Borges", "9789754706574", Genre.FANTASIA),
                book("El túnel", "Ernesto Sabato", "9789871144266", Genre.MISTERIO),
                book("Pedro Páramo", "Juan Rulfo", "9798663628426", Genre.MISTERIO),
                book("La ciudad y los perros", "Mario Vargas Llosa", "9789681903978", Genre.CLASICA),
                book("Veinte poemas de amor y una canción desesperada", "Pablo Neruda", "9788416259595", Genre.POESIA),
                book("Orgullo y prejuicio", "Jane Austen", "9788415083696", Genre.ROMANTICA),
                book("Cumbres borrascosas", "Emily Brontë", "9798708166524", Genre.ROMANTICA),
                book("Moby Dick", "Herman Melville", "9781400092727", Genre.AVENTURAS),
                book("Crimen y castigo", "Fiódor Dostoyevski", "9788418008122", Genre.CLASICA),
                book("1984", "George Orwell", "9789899135024", Genre.CIENCIA_FICCION),
                book("El gran Gatsby", "Francis Scott Fitzgerald", "9788429761221", Genre.CLASICA),
                book("Matar a un ruiseñor", "Harper Lee", "9780060935467", Genre.CLASICA),
                book("El guardián entre el centeno", "J.D. Salinger", "9781613839560", Genre.CLASICA),
                book("Los miserables", "Victor Hugo", "9781115828161", Genre.HISTORICA),
                book("Ana Karenina", "León Tolstói", "9780141199610", Genre.CLASICA),
                book("El viejo y el mar", "Ernest Hemingway", "9781541324565", Genre.AVENTURAS),
                book("Fahrenheit 451", "Ray Bradbury", "9780745171067", Genre.CIENCIA_FICCION)
        );
    }

    private static SaveBookDto book(String title, String author, String isbn, Genre genre) {
        return new SaveBookDto(title, author, isbn, genre);
    }
}
