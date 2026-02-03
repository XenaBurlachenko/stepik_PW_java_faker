import com.github.javafaker.Faker;
import com.microsoft.playwright.Browser;  
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

public class FakerGenerTest {  
    public static void main(String[] args) {  
        try (Playwright playwright = Playwright.create()) {  
            Browser browser = playwright.chromium().launch();  
            Page page = browser.newPage();
 
            Faker faker = new Faker();
            String generatedName = faker.name().fullName();
            System.out.println("Generated name: " + generatedName);

            page.route("**/dynamic_content", route -> {  
                route.fulfill(new Route.FulfillOptions()  
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody("[{\"id\": 1, \"content\": \"" + generatedName + "\", \"image\": \"/img/avatar-blank.jpg\"}]")
                );  
            });  

            page.navigate("https://the-internet.herokuapp.com/dynamic_content");  

            try {
                page.waitForSelector("div.row", new Page.WaitForSelectorOptions().setTimeout(5000));
            } catch (Exception e) {
                page.waitForTimeout(2000);
            }
            
            String pageContent = page.textContent("body");
            
            // Проверяем отображение имени
            if (pageContent.contains(generatedName)) {
                System.out.println("НЕ УПАЛ: Имя '" + generatedName + "' найдено на странице");
            } else {
                System.out.println("УПАЛ: Имя '" + generatedName + "' не найдено на странице");
                System.exit(1);
            }
            
            browser.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }  
}