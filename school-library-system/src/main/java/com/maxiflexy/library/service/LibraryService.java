package com.maxiflexy.library.service;

import com.maxiflexy.library.entity.Book;
import com.maxiflexy.library.entity.BookLoan;
import com.maxiflexy.library.entity.Student;
import com.maxiflexy.library.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LibraryService {

    private static final int MAX_LOANS_PER_STUDENT = 3;
    private static final int LOAN_DURATION_DAYS = 14;

    private final BookService bookService;
    private final StudentService studentService;
    private final BookLoanRepository bookLoanRepository;

    public BookLoan loanBook(Long bookId, Long studentId) {
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Check if book is available
        if (!bookService.isBookAvailable(bookId)) {
            throw new IllegalStateException("Book is not available for loan");
        }

        // Check student loan limit
        Long activeLoans = bookLoanRepository.countActiveLoansForStudent(student, BookLoan.LoanStatus.ACTIVE);
        if (activeLoans >= MAX_LOANS_PER_STUDENT) {
            throw new IllegalStateException("Student has reached maximum loan limit");
        }

        // Create loan
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(LOAN_DURATION_DAYS);
        BookLoan loan = new BookLoan(book, student, loanDate, dueDate);

        // Update book availability
        bookService.decrementAvailableCopies(book);

        return bookLoanRepository.save(loan);
    }

    public BookLoan returnBook(Long loanId) {
        BookLoan loan = bookLoanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != BookLoan.LoanStatus.ACTIVE) {
            throw new IllegalStateException("Loan is not active");
        }

        // Update loan
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(BookLoan.LoanStatus.RETURNED);

        // Update book availability
        bookService.incrementAvailableCopies(loan.getBook());

        return bookLoanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<BookLoan> findOverdueLoans() {
        return bookLoanRepository.findOverdueLoans(LocalDate.now(), BookLoan.LoanStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<BookLoan> findActiveLoansForStudent(Long studentId) {
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        return bookLoanRepository.findByStudentAndStatus(student, BookLoan.LoanStatus.ACTIVE);
    }

    public void updateOverdueLoans() {
        List<BookLoan> overdueLoans = findOverdueLoans();
        for (BookLoan loan : overdueLoans) {
            loan.setStatus(BookLoan.LoanStatus.OVERDUE);
        }
        bookLoanRepository.saveAll(overdueLoans);
    }
}
