package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Classe base do padrao Page Object Model (POM).
 * Centraliza operacoes comuns de navegacao e espera entre todas as paginas.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /** Aguarda um elemento ficar visivel e o retorna. */
    protected WebElement aguardarElementoVisivel(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Aguarda um elemento ficar clicavel e o retorna. */
    protected WebElement aguardarElementoClicavel(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Verifica se um elemento esta presente na pagina. */
    protected boolean elementoEstaPresente(By locator) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Retorna o titulo da pagina atual. */
    public String getTituloPagina() {
        return driver.getTitle();
    }

    /** Retorna a URL atual do navegador. */
    public String getUrlAtual() {
        return driver.getCurrentUrl();
    }

    /** Navega para uma URL especifica. */
    public void navegarPara(String url) {
        driver.get(url);
    }

    /** Verifica se um alerta de sucesso esta visivel na pagina. */
    public boolean alertaSucessoVisivel() {
        return elementoEstaPresente(By.id("alertSucesso"));
    }

    /** Verifica se um alerta de erro esta visivel na pagina. */
    public boolean alertaErroVisivel() {
        return elementoEstaPresente(By.id("alertErro"));
    }

    /** Retorna o texto do alerta de sucesso. */
    public String getTextoAlertaSucesso() {
        return aguardarElementoVisivel(By.id("alertSucesso")).getText();
    }

    /** Retorna o texto do alerta de erro. */
    public String getTextoAlertaErro() {
        return aguardarElementoVisivel(By.id("alertErro")).getText();
    }
}
