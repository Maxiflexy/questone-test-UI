package com.maxiflexy.library.repository;

import com.maxiflexy.library.config.AbstractIntegrationTest;
import com.maxiflexy.library.entity.Book;
import com.maxiflexy.library.entity.BookLoan;
import com.maxiflexy.library.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookLoanRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BookLoanRepository bookLoanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void findByStudentAndStatus_shouldReturnActiveLoans() {
        // Given
        Book book = new Book("Test Book", "Test Author", "978-1-2345-6789-0", 3, 5);
        Student student = new Student("STU001", "John", "Doe", "john.doe@example.com");

        bookRepository.save(book);
        studentRepository.save(student);

        BookLoan activeLoan1 = new BookLoan(book, student, LocalDate.now(), LocalDate.now().plusDays(14));
        BookLoan activeLoan2 = new BookLoan(book, student, LocalDate.now(), LocalDate.now().plusDays(7));

        bookLoanRepository.save(activeLoan1);
        bookLoanRepository.save(activeLoan2);

        // When
        List<BookLoan> loans = bookLoanRepository.findByStudentAndStatus(student, BookLoan.LoanStatus.ACTIVE);

        // Then
        assertThat(loans).hasSize(2);
        assertThat(loans).allMatch(loan -> loan.getStatus() == BookLoan.LoanStatus.ACTIVE);
    }

    @Test
    void findOverdueLoans_shouldReturnOverdueLoans() {
        // Given
        Book book = new Book("Test Book", "Test Author", "978-1-2345-6789-1", 3, 5);
        Student student = new Student("STU002", "Jane", "Smith", "jane.smith@example.com");

        bookRepository.save(book);
        studentRepository.save(student);

        BookLoan overdueLoan = new BookLoan(book, student,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));
        overdueLoan.setStatus(BookLoan.LoanStatus.ACTIVE);

        bookLoanRepository.save(overdueLoan);

        // When
        List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans(
                LocalDate.now(), BookLoan.LoanStatus.ACTIVE);

        // Then
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getDueDate()).isBefore(LocalDate.now());
    }

    @Test
    void countActiveLoansForStudent_shouldReturnCorrectCount() {
        // Given
        Book book = new Book("Test Book", "Test Author", "978-1-2345-6789-2", 3, 5);
        Student student = new Student("STU003", "Bob", "Johnson", "bob.johnson@example.com");

        bookRepository.save(book);
        studentRepository.save(student);

        BookLoan loan1 = new BookLoan(book, student, LocalDate.now(), LocalDate.now().plusDays(14));
        BookLoan loan2 = new BookLoan(book, student, LocalDate.now(), LocalDate.now().plusDays(7));

        bookLoanRepository.save(loan1);
        bookLoanRepository.save(loan2);

        // When
        Long count = bookLoanRepository.countActiveLoansForStudent(student, BookLoan.LoanStatus.ACTIVE);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void save_shouldPersistLoan() {
        // Given
        Book book = new Book("Test Book", "Test Author", "978-1-2345-6789-3", 3, 5);
        Student student = new Student("STU004", "Alice", "Brown", "alice.brown@example.com");

        bookRepository.save(book);
        studentRepository.save(student);

        BookLoan newLoan = new BookLoan(book, student, LocalDate.now(), LocalDate.now().plusDays(7));

        // When
        BookLoan saved = bookLoanRepository.save(newLoan);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(BookLoan.LoanStatus.ACTIVE);
    }
}