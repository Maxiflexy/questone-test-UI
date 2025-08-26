package com.maxiflexy.library.controller;

import com.maxiflexy.library.SchoolLibraryApplication;
import com.maxiflexy.library.config.AbstractIntegrationTest;
import com.maxiflexy.library.entity.Book;
import com.maxiflexy.library.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SchoolLibraryApplication.class)
@AutoConfigureMockMvc
class BookControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        testBook = new Book("Test Title", "Test Author", "978-1-2345-6789-0", 5, 5);
        bookRepository.save(testBook);
    }


    @Test
    void createBook_shouldReturnCreated_whenValidBook() throws Exception {
        // Given
        Book newBook = new Book("New Book", "New Author", "978-1-2345-6789-1", 3, 3);

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Book")))
                .andExpect(jsonPath("$.author", is("New Author")))
                .andExpect(jsonPath("$.isbn", is("978-1-2345-6789-1")))
                .andExpect(jsonPath("$.availableCopies", is(3)));
    }

    @Test
    void createBook_shouldReturnBadRequest_whenInvalidBook() throws Exception {
        // Given - Book with missing required fields
        Book invalidBook = new Book();
        invalidBook.setTitle(""); // Invalid empty title

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBook_shouldReturnBook_whenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/{id}", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBook.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.author", is("Test Author")))
                .andExpect(jsonPath("$.isbn", is("978-1-2345-6789-0")));
    }


}