package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object para a pagina de listagem de produtos.
 * Encapsula a interacao com a tabela, botoes e alertas.
 */
public class ProdutoListagemPage extends BasePage {

    private static final By TABELA_PRODUTOS = By.id("tabelaProdutos");
    private static final By BTN_NOVO = By.id("btnNovo");
    private static final By LINHAS_TABELA = By.cssSelector("#tabelaProdutos tbody tr");
    private static final By SEM_REGISTROS = By.id("semRegistros");

    public ProdutoListagemPage(WebDriver driver) {
        super(driver);
    }

    public ProdutoFormularioPage clicarNovoProduto() {
        aguardarElementoClicavel(BTN_NOVO).click();
        return new ProdutoFormularioPage(driver);
    }

    public int contarLinhas() {
        if (!elementoEstaPresente(TABELA_PRODUTOS)) {
            return 0;
        }
        List<WebElement> linhas = driver.findElements(LINHAS_TABELA);
        return linhas.size();
    }

    public boolean mensagemSemRegistrosVisivel() {
        return elementoEstaPresente(SEM_REGISTROS);
    }

    public String getTextoCelula(int linha, int coluna) {
        String cssSelector = String.format("#tabelaProdutos tbody tr:nth-child(%d) td:nth-child(%d)", linha + 1, coluna + 1);
        return aguardarElementoVisivel(By.cssSelector(cssSelector)).getText();
    }

    public ProdutoFormularioPage clicarEditar(int linha) {
        String cssSelector = String.format("#tabelaProdutos tbody tr:nth-child(%d) .btn-editar", linha + 1);
        aguardarElementoClicavel(By.cssSelector(cssSelector)).click();
        return new ProdutoFormularioPage(driver);
    }

    public ProdutoListagemPage clicarExcluir(int linha) {
        String cssSelector = String.format("#tabelaProdutos tbody tr:nth-child(%d) .btn-excluir", linha + 1);
        aguardarElementoClicavel(By.cssSelector(cssSelector)).click();
        return this;
    }

    public ProdutoListagemPage confirmarExclusao() {
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // Alert pode ja ter sido processado
        }
        return this;
    }

    public boolean tabelaEstaVisivel() {
        return elementoEstaPresente(TABELA_PRODUTOS);
    }
}
