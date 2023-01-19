package example.selenium_cdp;

import org.openqa.selenium.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.annotations.*;

import static org.testng.Assert.*;

import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MainPageTest {
    private WebDriver driver;
    private DevTools devTools;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();
        this.pinJavaScript(devTools);
        driver.get("https://www.jetbrains.com/");
    }

    public void pinJavaScript(DevTools devTools) {
        devTools.getDomains().javascript().pin("notification", """
                import("https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js").then(()=>{
                    $.getScript('https://cdnjs.cloudflare.com/ajax/libs/jquery-jgrowl/1.4.8/jquery.jgrowl.min.js');
                    $('head').append('<link href="https://cdnjs.cloudflare.com/ajax/libs/jquery-jgrowl/1.4.8/jquery.jgrowl.min.css" rel="stylesheet" type="text/css" />');
                });
                function hightlight(element) {
                	const actual_back = element.style.background;
                	const actual_out = element.style.outline;

                	element.style.backgroundColor = 'yellow';
                	element.style.outline = '2px solid red';

                	setTimeout(()=>{
                		element.style.backgroundColor = actual_back;
                		element.style.outline = actual_out;
                	},1000);
                }
                """);

//        // or

// getting exception with below script  - org.openqa.selenium.JavascriptException: javascript error: $ is not defined (when running on website not having jquery - amazon.in)
//        devTools.getDomains().javascript().pin("notifications", """
//                window.onload = () => {
//                    if (!window.jQuery) {
//                        var jquery = document.createElement('script');
//                        jquery.type = 'text/javascript';
//                        jquery.src = 'https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js';
//                        document.getElementsByTagName('head')[0].appendChild(jquery);
//                    } else {
//                        $ = window.jQuery;
//                    }
//
//                    $.getScript('https://cdnjs.cloudflare.com/ajax/libs/jquery-jgrowl/1.4.8/jquery.jgrowl.min.js')
//                    $('head').append('<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-jgrowl/1.4.8/jquery.jgrowl.min.css" type="text/css" />');
//                }
//
//                function highlight(element){
//                    let defaultBG = element.style.backgroundColor;
//                    let defaultOutline = element.style.outline;
//                    element.style.backgroundColor = '#FDFF47';
//                    element.style.outline = '#f00 solid 2px';
//
//                    setTimeout(function()
//                    {
//                        element.style.backgroundColor = defaultBG;
//                        element.style.outline = defaultOutline;
//                    }, 1000);
//                }
//                """);
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void testAmazon() throws InterruptedException {

        driver.navigate().to("https://www.amazon.in");
        this.enterText(driver, By.cssSelector("[id='twotabsearchtextbox']"), "macbook");
        click(driver, By.cssSelector("[id='nav-search-submit-button']"));
        click(driver, By.xpath("(//span[contains(text(),'2020 Apple MacBook Air Laptop: Apple M1 chip, 13.3-inch/33.74 cm Retina Display, 8GB RAM, 256GB SSD Storage, Backlit Keyboard, FaceTime HD Camera, Touch ID. ')])[1]"));

        String current_window = driver.getWindowHandle();
        String new_winwodw = driver.getWindowHandles().stream().filter(win_handle -> !(current_window.equalsIgnoreCase(win_handle))).collect(Collectors.joining());
        driver.switchTo().window(new_winwodw);

//        DevTools devTools1 = ((ChromeDriver)driver).getDevTools();
//        devTools1.createSession(driver.getWindowHandle());
//        this.pinJavaScript(devTools1);
// neither above or below ways work..
        devTools.createSession(driver.getWindowHandle());
        this.pinJavaScript(devTools);
        Thread.sleep(3000);
        click(driver, By.cssSelector("div#imgTagWrapperId"));
        Thread.sleep(5000);
    }

    public void click(WebDriver driver, By locator) throws InterruptedException {
        waitForPageLoad();
        WebElement ele = this.waitForElement(driver, locator);
        this.growlMessage("clicking on > btn");
        this.highlight(ele, driver);
        Thread.sleep(1000);
        ele.click();
    }

    public void enterText(WebDriver driver, By locator, String text) throws InterruptedException {
        waitForPageLoad();
        WebElement ele = this.waitForElement(driver, locator);
        this.growlMessage("entering text >> " + text);
        this.highlight(ele, driver);
        Thread.sleep(1000);
        ele.sendKeys(text);
    }

    public void highlight(WebElement ele, WebDriver driver) {
        waitForPageLoad();
        ((JavascriptExecutor) driver).executeScript("hightlight(arguments[0]); arguments[0].scrollIntoView(true);", ele);
    }

    public WebElement waitForElement(WebDriver driver, By locator) {
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
        return wait.withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoreAll(Arrays.asList(NoSuchElementException.class))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void growlMessage(String msg) {
        waitForPageLoad();
        ((JavascriptExecutor) driver).executeScript(String.format("$.jGrowl(' %s ')", msg));
    }

    private void waitForPageLoad() {
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
        wait.withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(500))
                .until(d -> {
                    return (Boolean) ((JavascriptExecutor) d).executeScript("return document.readyState == 'complete'");
                });
    }
}
