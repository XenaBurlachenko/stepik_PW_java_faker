import com.github.javafaker.Faker;
import com.microsoft.playwright.Browser;  
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

public class FakerGenerTest {  
    public static void main(String[] args) {  
        try (Playwright playwright = Playwright.create()) {  
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));  
            Page page = browser.newPage();  

            // Генерируем данные  
            Faker faker = new Faker();  
            String generatedName = faker.name().fullName();  

            // Мокируем API  
            page.route("**/dynamic_content", route -> {  
                route.fulfill(new Route.FulfillOptions()  
                    .setStatus(200)  
                    .setContentType("application/json")  
                    .setBody("[{\"id\": 1, \"content\": \"" + generatedName + "\", \"image\": \"/img/avatar-blank.jpg\"}]")  
                );  
            });  

            // Запускаем тест  
            page.navigate("https://the-internet.herokuapp.com/dynamic_content");  
            page.waitForSelector(".large-10.columns");  
            
            String content = page.locator(".large-10.columns").first().textContent();  
            if (content.contains(generatedName)) {  
                System.out.println("✓ Тест пройден: имя '" + generatedName + "' отображается на странице!");  
            } else {  
                System.out.println("✗ Тест не пройден: имя '" + generatedName + "' не найдено!");  
            }  
            
            browser.close();  
        }  
    }  
}