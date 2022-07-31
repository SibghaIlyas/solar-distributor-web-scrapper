package webScrapper;

import com.google.api.services.sheets.v4.model.ValueRange;
import helpers.GoogleSheetHelpers;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

public class WebScrapper {

    private String fetchedPrice = null;
    private String range = "Product_Data!A1:E1000";
    private int totalRows = 0;

    private HashMap<String, String> data = new HashMap<String, String>();
    ChromeOptions options = new ChromeOptions();
    private WebDriver driver;
    private WebDriverWait wait;

    By newAccount = By.xpath("//div[@class='pr-newNavbar__account']");
    By email = By.xpath("//input[@name='lgn_usr']");
    By password = By.xpath("//input[@name='lgn_pwd']");
    By loginBtn = By.xpath("//button[text()='Log in']");
    By acceptAllCookies = By.xpath("//a[contains(text(),'Allow all cookies')]");

    public WebScrapper() throws IOException {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        options.addArguments("headless");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
    }

    public void login() {
        driver.get("https://solar-distribution.baywa-re.de/en/");
        System.out.println("landed on website");
        wait.until(ExpectedConditions.elementToBeClickable(acceptAllCookies));
        driver.findElement(acceptAllCookies).click();
        System.out.println("Accepted all cookies");
        driver.findElement(newAccount).click();
        driver.findElement(email).sendKeys("pm@ddv.gmbh");
        driver.findElement(password).sendKeys("!g67zeAq3zHUZdU");
        driver.findElement(loginBtn).click();
        System.out.println("logged into the system");
    }

    public void SolarDistributorWebScrapper() throws IOException, GeneralSecurityException, InterruptedException {

        int googleStatusCode = 1;
        int rowNo = 2;

        ValueRange sheetDataResponse = GoogleSheetHelpers.getData(range);
        List<List<Object>> dataList = sheetDataResponse.getValues();
        System.out.println(dataList);

        //get locators of all categories available
        List<WebElement> categories = driver.findElements(By.xpath("(//ul[@class='pr-newNavbar__menu-list'])[4]/li"));
        System.out.println(categories.size());
        int count = 1;
        while(count <= categories.size()) {
            driver.get("https://solar-distribution.baywa-re.de/en/");
            //get category name
            WebElement categoryElement = driver.findElement(By.xpath("((//ul[@class='pr-newNavbar__menu-list'])[4]/li)["+count+"]"));
            String title = driver.findElement(By.xpath("(((//ul[@class='pr-newNavbar__menu-list'])[4]/li)/a/div[@class='pr-newNavbar__menu-link-middle pr-newNavbar__menu-title'])["+count+"]")).getText();
            System.out.println(title);
            data.put("Category", title);
            wait.until(ExpectedConditions.elementToBeClickable(categoryElement));
            categoryElement.click();

            //get product name
            List<WebElement> products = driver.findElements(By.xpath("//div[@data-id='main-products-content']/div"));

            int productCount = 1;
            while(productCount <= products.size()) {
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#productList_1")));
                    String product = driver.findElement(By.cssSelector("#productList_" + productCount)).getText();
                    data.put("name", product);
                    String price = driver.findElement(By.xpath("//div[@id='productList_" + productCount + "']/following-sibling::div[@class='pr-productTiles__detail-price']/div")).getText();
                    data.put("price", price);
                    String articleNo = driver.findElement(By.xpath("//div[@id='productList_"+ productCount +"']/following-sibling::div[@class='pr-productTiles__detail-articlenr']")).getText();
                    data.put("articleNo", articleNo);
                    String URL = driver.findElement(By.xpath("//div[@id='productList_"+ productCount +"']/../preceding-sibling::a")).getAttribute("href");
                    data.put("URL", URL);
                    String status = driver.findElement(By.xpath("//div[@id='productList_" + productCount + "']/following-sibling::div[@class='pr-productTiles__detail-availability']")).getText();
                    data.put("status", status);

                    googleStatusCode = GoogleSheetHelpers.writeToSingleRange(data, rowNo);
                    if(googleStatusCode == 429) {
                        Thread.sleep(60000);
                    }
                    productCount++;
                    rowNo++;

                } catch (Exception e) {
                    try {
                        String status = driver.findElement(By.xpath("//div[@id='productList_" + productCount + "']/following-sibling::div[@class='pr-productTiles__info-red']")).getText();
                        data.put("status", status);
                        GoogleSheetHelpers.writeToSingleRange(data, rowNo);
                        System.out.println("e:" +  e);
                        productCount++;
                        rowNo++;
                    }
                    catch (Exception e1) {
                        data.put("name", "Product could not  be found");
                        data.put("price", "Product could not  be found");
                        data.put("status", "Product could not  be found");
                        System.out.println("e1:" +  e1);
                        GoogleSheetHelpers.writeToSingleRange(data, rowNo);
                        productCount++;
                        rowNo++;
                    }
                }
            }

            data.clear();
            count++;

        }
    }

}
