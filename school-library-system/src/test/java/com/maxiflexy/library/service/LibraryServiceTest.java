package com.maxiflexy.library.service;

import com.maxiflexy.library.entity.Book;
import com.maxiflexy.library.entity.BookLoan;
import com.maxiflexy.library.entity.Student;
import com.maxiflexy.library.repository.BookLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private StudentService studentService;

    @Mock
    private BookLoanRepository bookLoanRepository;

    @InjectMocks
    private LibraryService libraryService;

    private Book testBook;
    private Student testStudent;
    private BookLoan testLoan;

    @BeforeEach
    void setUp() {
        testBook = new Book("Test Title", "Test Author", "123-456-789", 5, 5);
        testBook.setId(1L);

        testStudent = new Student("STU001", "John", "Doe", "john.doe@example.com");
        testStudent.setId(1L);

        testLoan = new BookLoan(testBook, testStudent, LocalDate.now(), LocalDate.now().plusDays(14));
        testLoan.setId(1L);
    }

    @Test
    void loanBook_shouldCreateLoan_whenValidRequest() {
        // Given
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));
        when(studentService.findById(1L)).thenReturn(Optional.of(testStudent));
        when(bookService.isBookAvailable(1L)).thenReturn(true);
        when(bookLoanRepository.countActiveLoansForStudent(testStudent, BookLoan.LoanStatus.ACTIVE))
                .thenReturn(1L);
        when(bookLoanRepository.save(any(BookLoan.class))).thenReturn(testLoan);

        // When
        BookLoan result = libraryService.loanBook(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBook().getId()).isEqualTo(1L);
        assertThat(result.getStudent().getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookLoan.LoanStatus.ACTIVE);

        verify(bookService).findById(1L);
        verify(studentService).findById(1L);
        verify(bookService).isBookAvailable(1L);
        verify(bookService).decrementAvailableCopies(testBook);
        verify(bookLoanRepository).save(any(BookLoan.class));
    }

    @Test
    void loanBook_shouldThrowException_whenBookNotAvailable() {
        // Given
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));
        when(studentService.findById(1L)).thenReturn(Optional.of(testStudent));
        when(bookService.isBookAvailable(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> libraryService.loanBook(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book is not available for loan");

        verify(bookService, never()).decrementAvailableCopies(any());
        verify(bookLoanRepository, never()).save(any());
    }

    @Test
    void loanBook_shouldThrowException_whenStudentExceedsLoanLimit() {
        // Given
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));
        when(studentService.findById(1L)).thenReturn(Optional.of(testStudent));
        when(bookService.isBookAvailable(1L)).thenReturn(true);
        when(bookLoanRepository.countActiveLoansForStudent(testStudent, BookLoan.LoanStatus.ACTIVE))
                .thenReturn(3L); // Max loans reached

        // When & Then
        assertThatThrownBy(() -> libraryService.loanBook(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Student has reached maximum loan limit");

        verify(bookService, never()).decrementAvailableCopies(any());
        verify(bookLoanRepository, never()).save(any());
    }

    @Test
    void returnBook_shouldReturnBook_whenValidLoanId() {
        // Given
        when(bookLoanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(bookLoanRepository.save(any(BookLoan.class))).thenReturn(testLoan);

        // When
        BookLoan result = libraryService.returnBook(1L);

        // Then
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(result.getStatus()).isEqualTo(BookLoan.LoanStatus.RETURNED);

        verify(bookLoanRepository).findById(1L);
        verify(bookService).incrementAvailableCopies(testBook);
        verify(bookLoanRepository).save(testLoan);
    }

    @Test
    void returnBook_shouldThrowException_whenLoanNotActive() {
        // Given
        testLoan.setStatus(BookLoan.LoanStatus.RETURNED);
        when(bookLoanRepository.findById(1L)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThatThrownBy(() -> libraryService.returnBook(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Loan is not active");

        verify(bookService, never()).incrementAvailableCopies(any());
    }

    @Test
    void findOverdueLoans_shouldReturnOverdueLoans() {
        // Given
        BookLoan overdueLoan = new BookLoan(testBook, testStudent,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));
        List<BookLoan> overdueLoans = Arrays.asList(overdueLoan);

        when(bookLoanRepository.findOverdueLoans(any(LocalDate.class), eq(BookLoan.LoanStatus.ACTIVE)))
                .thenReturn(overdueLoans);

        // When
        List<BookLoan> result = libraryService.findOverdueLoans();

        // Then
        assertThat(result).hasSize(1);
        verify(bookLoanRepository).findOverdueLoans(LocalDate.now(), BookLoan.LoanStatus.ACTIVE);
    }

    @Test
    void findActiveLoansForStudent_shouldReturnActiveLoans() {
        // Given
        List<BookLoan> activeLoans = Arrays.asList(testLoan);
        when(studentService.findById(1L)).thenReturn(Optional.of(testStudent));
        when(bookLoanRepository.findByStudentAndStatus(testStudent, BookLoan.LoanStatus.ACTIVE))
                .thenReturn(activeLoans);

        // When
        List<BookLoan> result = libraryService.findActiveLoansForStudent(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(studentService).findById(1L);
        verify(bookLoanRepository).findByStudentAndStatus(testStudent, BookLoan.LoanStatus.ACTIVE);
    }
}
