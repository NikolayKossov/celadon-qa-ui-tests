package pages;

import io.qameta.allure.Step;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CeladonVacancyPage {
    public static final String DEFAULT_URL = "https://script.google.com/macros/s/AKfycbz8yTu7kqG8DrmWSIqzn4-ze9Q-iQHKVuNzvvL8NFYAyvKeIsmabCWvBGnCsjUTHL-aqw/exec";

    @Step("Open vacancy and switch through Google Apps Script frames")
    public CeladonVacancyPage openPage() {
        open(System.getProperty("celadon.url", DEFAULT_URL));
        switchTo().frame($("#sandboxFrame").shouldBe(visible));
        switchTo().frame($("#userHtmlFrame").shouldBe(visible));
        $("h1").shouldBe(visible).shouldHave(exactText("Junior QA"));
        return this;
    }

    @Step("Verify visible vacancy information: {expected}")
    public CeladonVacancyPage shouldShow(String expected) {
        $(byText(expected)).shouldBe(visible);
        return this;
    }

    @Step("Verify {label} link in {scope} points to {href}")
    public CeladonVacancyPage shouldHaveLink(String scope, String label, String href) {
        $(scope).$$("a").findBy(exactText(label))
                .shouldBe(visible).shouldHave(attribute("href", href));
        return this;
    }

    @Step("Verify application email and prefilled subject without sending mail")
    public CeladonVacancyPage shouldHaveApplicationLink() {
        $("h2").shouldHave(exactText("Ready to Apply?"));
        String href = $("a[href^='mailto:'][href*='subject=']").shouldBe(visible)
                .shouldHave(exactText("yuliya.krivosheeva@celadonsoft.com")).getAttribute("href");
        assertEquals("mailto:yuliya.krivosheeva@celadonsoft.com?subject=QA_Surname First Name",
                URLDecoder.decode(href, StandardCharsets.UTF_8));
        shouldShow("QA_Surname First Name");
        return this;
    }
}
