import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

public class FakerGenerTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    Faker faker;
    String generatedName;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        faker = new Faker();
        generatedName = faker.name().fullName();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testDynamicContentWithMockedAPI() {
        // Настройка мокирования API
        page.route("**/dynamic_content", route -> {
            // Создаем массив из 3 пользователей с нашим сгенерированным именем
            String mockResponse = "[" +
                "{\"id\": 1, \"content\": \"" + generatedName + "\", \"image\": \"/img/avatar-blank.jpg\"}," +
                "{\"id\": 2, \"content\": \"" + faker.name().fullName() + "\", \"image\": \"/img/avatar-blank.jpg\"}," +
                "{\"id\": 3, \"content\": \"" + faker.name().fullName() + "\", \"image\": \"/img/avatar-blank.jpg\"}" +
                "]";
            
            route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(mockResponse));
        });

        // Переход на страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_content");
        
        // Ожидание загрузки контента
        page.waitForSelector(".row:has(.large-10.columns)");
        
        // Проверка, что сгенерированное имя присутствует на странице
        String pageText = page.textContent("body");
        assertNotNull(pageText, "Содержимое страницы не должно быть null");
        assertTrue(pageText.contains(generatedName), 
            "Страница должна содержать сгенерированное имя: " + generatedName);
        
        System.out.println("Успешно! Имя '" + generatedName + "' найдено на странице.");
    }
}