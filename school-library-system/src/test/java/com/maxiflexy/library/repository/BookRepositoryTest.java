package com.maxiflexy.library.repository;

import com.maxiflexy.library.config.AbstractIntegrationTest;
import com.maxiflexy.library.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByIsbn_shouldReturnBook_whenIsbnExists() {
        // Given
        Book testBook = new Book("Spring Boot Guide", "John Smith", "978-1-2345-6789-0", 3, 5);
        bookRepository.save(testBook);

        // When
        Optional<Book> found = bookRepository.findByIsbn("978-1-2345-6789-0");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot Guide");
    }

    @Test
    void findByIsbn_shouldReturnEmpty_whenIsbnNotExists() {
        // When
        Optional<Book> found = bookRepository.findByIsbn("non-existent-isbn");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByAuthorContainingIgnoreCase_shouldReturnMatchingBooks() {
        // Given
        Book testBook = new Book("Spring Boot Guide", "John Smith", "978-1-2345-6789-0", 3, 5);
        bookRepository.save(testBook);

        // When
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase("john");

        // Then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor()).isEqualTo("John Smith");
    }

    @Test
    void findAvailableBooks_shouldReturnOnlyAvailableBooks() {
        // Given
        Book availableBook = new Book("Available Book", "Author", "978-1-1111-1111-1", 3, 5);
        Book unavailableBook = new Book("Unavailable Book", "Author", "978-2-2222-2222-2", 0, 2);
        bookRepository.save(availableBook);
        bookRepository.save(unavailableBook);

        // When
        List<Book> availableBooks = bookRepository.findAvailableBooks();

        // Then
        assertThat(availableBooks).hasSize(1);
        assertThat(availableBooks.get(0).getAvailableCopies()).isGreaterThan(0);
    }

    @Test
    void searchBooks_shouldReturnPagedResults() {
        // Given
        Book javaBook = new Book("Java Programming", "Jane Doe", "978-1-2345-6789-1", 2, 3);
        bookRepository.save(javaBook);
        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<Book> result = bookRepository.searchBooks("Java", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Java");
    }

    @Test
    void countUnavailableBooks_shouldReturnCorrectCount() {
        // Given
        Book availableBook = new Book("Available", "Author", "978-1-1111-1111-1", 1, 1);
        Book unavailableBook = new Book("Unavailable", "Author", "978-2-2222-2222-2", 0, 1);
        bookRepository.save(availableBook);
        bookRepository.save(unavailableBook);

        // When
        Long count = bookRepository.countUnavailableBooks();

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void save_shouldPersistBook() {
        // Given
        Book newBook = new Book("New Book", "New Author", "978-1-2345-6789-2", 1, 1);

        // When
        Book saved = bookRepository.save(newBook);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("New Book");
    }
}