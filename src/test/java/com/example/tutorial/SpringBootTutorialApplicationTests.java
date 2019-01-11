package com.example.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.tutorial.model.Book;

import io.restassured.RestAssured;
import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootTutorialApplication.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
public class SpringBootTutorialApplicationTests {

	private static final String API_ROOT = "http://localhost:8081/api/books";
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootTutorialApplicationTests.class);

	private Book createRandomBook() {
		Book book = new Book();
		book.setTitle(RandomStringUtils.randomAlphabetic(10));
		book.setAuthor(RandomStringUtils.randomAlphabetic(15));
		
		return book;
	}

	private static String createBookAsUri(Book book) {
		Response response = RestAssured.given().contentType(MediaType.APPLICATION_JSON_VALUE).body(book).post(API_ROOT);
		return API_ROOT + "/" + response.jsonPath().get("id");
	}

	@Test
	@Repeat(value = 10)
	public void whenGetCreatedBookById_thenOK() {
		Book book = createRandomBook();
		String location = createBookAsUri(book);
		Response response = RestAssured.get(location);
		
		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		assertEquals(book.getTitle(), response.jsonPath().get("title"));
		LOGGER.info(location + " (GetCreatedBookById) -->> " + response.jsonPath().get("title"));
	}

	@Test
	public void whenGetNotExistBookById_thenNotFound() {
		String location = API_ROOT + "/" + RandomStringUtils.randomNumeric(4);
		Response response = RestAssured.get(location);

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
	}

	@Test
	public void whenCreateNewBook_thenCreated() {
	    Book book = createRandomBook();
	    Response response = RestAssured.given()
	      .contentType(MediaType.APPLICATION_JSON_VALUE)
	      .body(book)
	      .post(API_ROOT);
	     
		assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
	}
	 
	@Test
	public void whenGetAllBooks_thenOK() {
		Response response = RestAssured.get(API_ROOT);

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		LOGGER.info(API_ROOT + " (GetAllBooks): " + response.jsonPath().prettify());
	}

	@Test
	public void whenGetBooksByTitle_thenOK() {
		String location = API_ROOT + "/title/" + "Book Title";
		Response response = RestAssured.get(location);

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		assertTrue(response.as(List.class).size() > 0);
		LOGGER.info(location + " (GetBooksByTitle): " + response.jsonPath().prettify());
	}

	@Test
	public void whenUpdateCreatedBook_thenUpdated() {
	    Book book = createRandomBook();
	    String location = createBookAsUri(book);
	    book.setId(Long.parseLong(location.split("api/books/")[1]));
	    book.setAuthor("New Author");
	    book.setTitle("Book Title");
	    Response response = RestAssured.given()
	      .contentType(MediaType.APPLICATION_JSON_VALUE)
	      .body(book)
	      .put(location);

	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
	 
	    response = RestAssured.get(location);
	     
	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
	    assertEquals("New Author", response.jsonPath().get("author"));
		LOGGER.info(location + " (UpdateCreatedBook): " + response.jsonPath().prettify());
	}
	
	@Test
	public void whenDeleteCreatedBook_thenOk() {
	    Book book = createRandomBook();
	    String location = createBookAsUri(book);
	    Response response = RestAssured.delete(location);
	     
	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
	 
	    response = RestAssured.get(location);
	    assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
	}
	
	@Test
	public void whenInvalidBook_thenError() {
	    Book book = createRandomBook();
	    book.setAuthor(null);
	    Response response = RestAssured.given()
	      .contentType(MediaType.APPLICATION_JSON_VALUE)
	      .body(book)
	      .post(API_ROOT);

	    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
	}

}