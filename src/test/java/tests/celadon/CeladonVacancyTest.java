package tests.celadon;

import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pages.CeladonVacancyPage;

@Epic("Celadon careers")
@Feature("Junior QA vacancy")
@DisplayName("Celadon Junior QA vacancy")
public class CeladonVacancyTest extends CeladonTestBase {
    private CeladonVacancyPage page;

    @BeforeEach
    void openVacancy() {
        page = new CeladonVacancyPage().openPage();
    }

    @Test
    @DisplayName("Vacancy title, location and employment type")
    @Severity(SeverityLevel.CRITICAL)
    void vacancySummary() {
        page.shouldShow("Remote · Full-time").shouldShow("Poland").shouldShow("B1+");
    }

    @Test
    @DisplayName("Required QA skills and optional experience")
    void requirements() {
        page.shouldShow("GUI, UI/UX testing").shouldShow("API testing")
                .shouldShow("web server architecture").shouldShow("B1 level or higher")
                .shouldShow("Six months or more in testing web resources and mobile applications");
    }

    @Test
    @DisplayName("All six advertised benefits are visible")
    void benefits() {
        page.shouldShow("100% Remote work").shouldShow("Flexible working hours")
                .shouldShow("25 vacation days + 5 sick days").shouldShow("Salary set after interview")
                .shouldShow("Modern equipment provided").shouldShow("Interesting projects, own ideas welcome");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Apply link contains the correct recipient and subject")
    void applicationEmail() {
        page.shouldHaveApplicationLink();
    }

    @ParameterizedTest(name = "{1} links to {2}")
    @CsvSource({
        "nav, Cases, https://celadonsoft.com/cases",
        "nav, About, https://celadonsoft.com/company",
        "footer, Website, https://celadonsoft.com/",
        "footer, LinkedIn, https://www.linkedin.com/company/celadon-soft",
        "footer, Privacy Policy, https://celadonsoft.com/privacy-policy",
        "footer, @yuliakriv, https://t.me/yuliakriv",
        "footer, yuliya.krivosheeva@celadonsoft.com, mailto:yuliya.krivosheeva@celadonsoft.com"
    })
    @DisplayName("Navigation and contact destinations")
    void links(String scope, String label, String destination) {
        page.shouldHaveLink(scope, label, destination);
    }
}
