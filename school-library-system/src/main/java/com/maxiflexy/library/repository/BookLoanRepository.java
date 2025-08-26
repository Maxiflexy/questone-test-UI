package com.maxiflexy.library.repository;

import com.maxiflexy.library.entity.BookLoan;
import com.maxiflexy.library.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    List<BookLoan> findByStudentAndStatus(Student student, BookLoan.LoanStatus status);

    List<BookLoan> findByStatus(BookLoan.LoanStatus status);

    @Query("SELECT bl FROM BookLoan bl WHERE bl.dueDate < :date AND bl.status = :status")
    List<BookLoan> findOverdueLoans(@Param("date") LocalDate date,
                                    @Param("status") BookLoan.LoanStatus status);

    @Query("SELECT COUNT(bl) FROM BookLoan bl WHERE bl.student = :student AND bl.status = :status")
    Long countActiveLoansForStudent(@Param("student") Student student,
                                    @Param("status") BookLoan.LoanStatus status);
}
