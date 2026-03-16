import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

/**
 * Тест для проверки возможности динамической подмены контента на странице.
 * 
 * @author Xena
 * @version documented
 */
public class FakerGenerTest {
    
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private Faker faker;
    
    /**
     * Настройка тестового окружения перед каждым тестом.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("=== Настройка теста ===");
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
        faker = new Faker();
    }
    
    /**
     * Очистка после каждого теста.
     */
    @AfterEach
    public void tearDown() {
        System.out.println("=== Очистка теста ===");
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
    
    /**
     * Тест проверяет подмену динамического контента.
     */
    @Test
    @DisplayName("Подмена динамического контента сгенерированными данными")
    public void testDynamicContentSubstitution() {
        System.out.println("Запуск теста...");
        
        // 1. Генерация тестовых данных
        String generatedName = faker.name().fullName();
        System.out.println("Сгенерировано имя: " + generatedName);
        assertNotNull(generatedName, "Имя не должно быть null");
        
        // 2. Перехват запроса
        page.route("**/dynamic_content", route -> {
            String mockResponse = "[{\"id\": 1, \"content\": \"" + generatedName + "\", \"image\": \"/img/avatar-blank.jpg\"}]";
            route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(mockResponse)
            );
            System.out.println("✓ Запрос перехвачен, ответ подменён");
        });
        
        // 3. Навигация
        System.out.println("Переход на страницу...");
        page.navigate("https://the-internet.herokuapp.com/dynamic_content");
        
        // 4. Ожидание загрузки
        try {
            page.waitForSelector("div.row", new Page.WaitForSelectorOptions().setTimeout(5000));
            System.out.println("✓ Контент загружен");
        } catch (Exception e) {
            System.out.println("⚠ Ожидание по таймеру...");
            page.waitForTimeout(2000);
        }
        
        // 5. Проверка
        String pageContent = page.textContent("body");
        boolean nameFound = pageContent.contains(generatedName);
        
        System.out.println("Искомое имя: " + generatedName);
        System.out.println("Найдено на странице: " + (nameFound ? "✓ ДА" : "✗ НЕТ"));
        
        // JUnit проверка
        assertTrue(nameFound, 
            "Сгенерированное имя '" + generatedName + "' должно отображаться на странице");
        
        System.out.println("✓ Тест успешно завершён");
    }
}