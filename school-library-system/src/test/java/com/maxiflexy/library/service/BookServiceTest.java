package com.maxiflexy.library.service;

import com.maxiflexy.library.entity.Book;
import com.maxiflexy.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book("Test Title", "Test Author", "123-456-789", 5, 5);
        testBook.setId(1L);
    }

    @Test
    void saveBook_shouldSaveSuccessfully_whenValidBook() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.saveBook(testBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Title");
        verify(bookRepository).findByIsbn("123-456-789");
        verify(bookRepository).save(testBook);
    }

    @Test
    void saveBook_shouldThrowException_whenBookWithIsbnExists() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));

        // When & Then
        assertThatThrownBy(() -> bookService.saveBook(testBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book with ISBN 123-456-789 already exists");

        verify(bookRepository).findByIsbn("123-456-789");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnBook_whenBookExists() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(bookRepository).findById(1L);
    }

    @Test
    void findAvailableBooks_shouldReturnAvailableBooks() {
        // Given
        List<Book> availableBooks = Arrays.asList(testBook);
        when(bookRepository.findAvailableBooks()).thenReturn(availableBooks);

        // When
        List<Book> result = bookService.findAvailableBooks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableCopies()).isPositive();
        verify(bookRepository).findAvailableBooks();
    }

    @Test
    void searchBooks_shouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(testBook));
        when(bookRepository.searchBooks("test", pageable)).thenReturn(bookPage);

        // When
        Page<Book> result = bookService.searchBooks("test", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(bookRepository).searchBooks("test", pageable);
    }

    @Test
    void updateBookCopies_shouldUpdateSuccessfully_whenBookExists() {
        // Given
        testBook.setAvailableCopies(3); // 2 copies are loaned
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.updateBookCopies(1L, 10);

        // Then
        assertThat(result.getTotalCopies()).isEqualTo(10);
        assertThat(result.getAvailableCopies()).isEqualTo(8); // 10 - 2 loaned
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);
    }

    @Test
    void isBookAvailable_shouldReturnTrue_whenCopiesAvailable() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = bookService.isBookAvailable(1L);

        // Then
        assertThat(result).isTrue();
        verify(bookRepository).findById(1L);
    }

    @Test
    void decrementAvailableCopies_shouldDecrementSuccessfully() {
        // Given
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        bookService.decrementAvailableCopies(testBook);

        // Then
        assertThat(testBook.getAvailableCopies()).isEqualTo(4);
        verify(bookRepository).save(testBook);
    }

    @Test
    void decrementAvailableCopies_shouldThrowException_whenNoCopiesAvailable() {
        // Given
        testBook.setAvailableCopies(0);

        // When & Then
        assertThatThrownBy(() -> bookService.decrementAvailableCopies(testBook))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No available copies for book");

        verify(bookRepository, never()).save(any());
    }
}