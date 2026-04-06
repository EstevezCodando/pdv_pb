package com.pdv.pontovenda.test;

import com.pdv.pontovenda.page.LoginPage;
import com.pdv.pontovenda.page.VendaPage;
import com.pdv.pontovenda.test.support.SeleniumDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PosDeploySeleniumTest")
class PosDeploySeleniumTest {

    private WebDriver driver;
    private String baseUrl;

    @BeforeEach
    void configurar() {
        baseUrl = System.getenv().getOrDefault("PDV_BASE_URL", "http://127.0.0.1:8080");

        SeleniumDriverFactory.prepararDriver();
        driver = SeleniumDriverFactory.criarDriverHeadless();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        String adminUser = System.getenv().getOrDefault("PDV_ADMIN_USER", "admin@pdv.com");
        String adminPass = System.getenv().getOrDefault("PDV_ADMIN_PASS", "admin123");

        driver.get(baseUrl + "/login");
        new LoginPage(driver).login(adminUser, adminPass);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Deve validar a navegacao basica da aplicacao publicada")
    void deveValidarAplicacaoPublicada() {
        driver.get(baseUrl);
        assertTrue(driver.getTitle().toLowerCase().contains("pdv") || driver.getPageSource().contains("Ponto de Venda"));

        driver.get(baseUrl + "/usuarios");
        assertTrue(driver.getPageSource().contains("Usuarios") || driver.getPageSource().contains("usu"));

        driver.get(baseUrl + "/produtos");
        assertTrue(driver.getPageSource().contains("Produtos") || driver.getPageSource().contains("prod"));
    }

    @Test
    @DisplayName("Deve acessar historico de vendas e formulario de nova venda")
    void deveAcessarPaginaDeVendas() {
        VendaPage vendaPage = new VendaPage(driver);

        driver.get(baseUrl + "/vendas");
        assertTrue(vendaPage.historicoCarregou(), "Pagina de historico de vendas nao carregou");

        driver.get(baseUrl + "/vendas/nova");
        assertTrue(vendaPage.formularioCarregou(), "Formulario de nova venda nao carregou");
    }
}
