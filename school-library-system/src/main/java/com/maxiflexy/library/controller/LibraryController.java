package com.maxiflexy.library.controller;


import com.maxiflexy.library.entity.BookLoan;
import com.maxiflexy.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping("/loan")
    public ResponseEntity<BookLoan> loanBook(@RequestParam Long bookId,
                                             @RequestParam Long studentId) {
        try {
            BookLoan loan = libraryService.loanBook(bookId, studentId);
            return new ResponseEntity<>(loan, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/return/{loanId}")
    public ResponseEntity<BookLoan> returnBook(@PathVariable Long loanId) {
        try {
            BookLoan loan = libraryService.returnBook(loanId);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/loans/overdue")
    public ResponseEntity<List<BookLoan>> getOverdueLoans() {
        List<BookLoan> loans = libraryService.findOverdueLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/loans/student/{studentId}")
    public ResponseEntity<List<BookLoan>> getActiveLoansForStudent(@PathVariable Long studentId) {
        try {
            List<BookLoan> loans = libraryService.findActiveLoansForStudent(studentId);
            return ResponseEntity.ok(loans);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/loans/update-overdue")
    public ResponseEntity<Void> updateOverdueLoans() {
        libraryService.updateOverdueLoans();
        return ResponseEntity.ok().build();
    }
}
