package stepDefinitions;

import apiEngine.model.Book;
import apiEngine.model.requests.AddBooksRequest;
import apiEngine.model.requests.AuthorizationRequest;
import apiEngine.model.requests.ISBN;
import apiEngine.model.requests.RemoveBookRequest;
import apiEngine.model.responses.Books;
import apiEngine.model.responses.Token;
import apiEngine.model.responses.UserAccount;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

public class Steps {
    private static final String USER_ID = "569d59aa-507e-4be1-8a3a-b94e7ebf0e12";
    private static final String USERNAME = "QA-Test-002";
    private static final String PASSWORD = "Test@@123";
    private static final String BASE_URL = "https://demoqa.com";

    private static Token tokenResponse;
    private static Book book;
    private static Response response;
    private static String jsonString;
    private static String bookId;


    @Given("I am an authorized user")
    public void iAmAnAuthorizedUser() {
        AuthorizationRequest authRequest = new AuthorizationRequest(USERNAME, PASSWORD);
        RestAssured.baseURI = BASE_URL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json")
        .header("accept","application/json");

        response = request.body(authRequest).post("/Account/v1/GenerateToken");

        Assert.assertEquals(response.getStatusCode(),200);
        tokenResponse = response.getBody().as(Token.class);
    }

    @Given("A list of books are available")
    public void listOfBooksAreAvailable() {
        RestAssured.baseURI = BASE_URL;
        RequestSpecification request = RestAssured.given();
        response = request.get("/BookStore/v1/Books");
//        jsonString = response.asString();
//        List<Map<String, String>> books = JsonPath.from(jsonString).get("books");
//        Assert.assertTrue(books.size() > 0);
//        bookId = books.get(0).get("isbn");
        Books books = response.getBody().as(Books.class);
        book = books.books.get(0);
    }

    @When("I add a book to my reading list")
    public void addBookInList() {
        ISBN collectionISBN = new ISBN(bookId);
        AddBooksRequest addBooksRequest = new AddBooksRequest(USER_ID, collectionISBN);
        RestAssured.baseURI = BASE_URL;
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + tokenResponse.token)
                .header("Content-Type", "application/json");

        response = request.body(addBooksRequest).post("/BookStore/v1/Books");
    }

    @Then("The book is added")
    public void bookIsAdded() {
        Assert.assertEquals(201, response.getStatusCode());

        UserAccount userAccount = response.getBody().as(UserAccount.class);
        Assert.assertEquals(USER_ID, userAccount.userId);
        Assert.assertEquals(book.isbn, userAccount.books.get(0).isbn);

    }

    @When("I remove a book from my reading list")
    public void removeBookFromList() {
        RemoveBookRequest removeBook = new RemoveBookRequest(bookId, USER_ID);
        RestAssured.baseURI = BASE_URL;
        RequestSpecification request = RestAssured.given();

        request.header("Authorization", "Bearer " + tokenResponse.token)
                .header("Content-Type", "application/json");

        response = request.body(removeBook).delete("/BookStore/v1/Book");
    }

    @Then("The book is removed")
    public void bookIsRemoved() {
        Assert.assertEquals(204, response.getStatusCode());

        RestAssured.baseURI = BASE_URL;
        RequestSpecification request = RestAssured.given();

        request.header("Authorization", "Bearer " + tokenResponse.token)
                .header("Content-Type", "application/json");

        response = request.get("/Account/v1/User/" + USER_ID);
        Assert.assertEquals(200, response.getStatusCode());

        jsonString = response.asString();
        List<Map<String, String>> booksOfUser = JsonPath.from(jsonString).get("books");
        Assert.assertEquals(0, booksOfUser.size());
    }
}