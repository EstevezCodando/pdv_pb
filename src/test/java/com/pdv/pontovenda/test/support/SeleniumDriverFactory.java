package com.pdv.pontovenda.test.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Fabrica centralizada de WebDriver para execucao local, CI e Docker.
 */
public final class SeleniumDriverFactory {

    private static final String VARIAVEL_CHROME_BIN = "CHROME_BIN";
    private static final String VARIAVEL_CHROMEDRIVER_PATH = "CHROMEDRIVER_PATH";

    private SeleniumDriverFactory() {
    }

    public static void prepararDriver() {
        final String caminhoDriver = System.getenv(VARIAVEL_CHROMEDRIVER_PATH);
        if (caminhoDriver != null && !caminhoDriver.isBlank()) {
            System.setProperty("webdriver.chrome.driver", caminhoDriver);
            return;
        }
        WebDriverManager.chromedriver().setup();
    }

    public static WebDriver criarDriverHeadless() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        final String chromeBin = System.getenv(VARIAVEL_CHROME_BIN);
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }

        return new ChromeDriver(options);
    }
}
