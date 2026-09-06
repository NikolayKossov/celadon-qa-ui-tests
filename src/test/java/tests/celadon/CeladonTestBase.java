package tests.celadon;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.ByteArrayInputStream;
import static com.codeborne.selenide.Selenide.closeWebDriver;

@Tag("celadon")
public abstract class CeladonTestBase {
    @BeforeAll
    static void configureBrowser() {
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserSize = System.getProperty("browser_size", "1440x1000");
        Configuration.browserVersion = System.getProperty("browser_version", "");
        Configuration.remote = System.getProperty("remote_url");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        Configuration.timeout = 20000;
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterEach
    void attachEvidenceAndClose() {
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                try {
                    Allure.addAttachment("Final screenshot", "image/png", new ByteArrayInputStream(
                            ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES)), "png");
                    Allure.addAttachment("Application frame HTML", "text/html",
                            WebDriverRunner.getWebDriver().getPageSource(), ".html");
                } catch (RuntimeException error) {
                    Allure.addAttachment("Evidence capture error", error.toString());
                }
            }
        } finally {
            closeWebDriver();
        }
    }
}
