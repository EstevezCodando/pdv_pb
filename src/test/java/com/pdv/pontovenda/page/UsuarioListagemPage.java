package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object para a pagina de listagem de usuarios.
 * Encapsula a interacao com a tabela, botoes e alertas de confirmacao.
 */
public class UsuarioListagemPage extends BasePage {

    private static final By TABELA_USUARIOS = By.id("tabelaUsuarios");
    private static final By BTN_NOVO = By.id("btnNovo");
    private static final By LINHAS_TABELA = By.cssSelector("#tabelaUsuarios tbody tr");
    private static final By SEM_REGISTROS = By.id("semRegistros");

    public UsuarioListagemPage(WebDriver driver) {
        super(driver);
    }

    /** Clica no botao "Novo Usuario" para abrir o formulario de cadastro. */
    public UsuarioFormularioPage clicarNovoUsuario() {
        aguardarElementoClicavel(BTN_NOVO).click();
        return new UsuarioFormularioPage(driver);
    }

    /** Retorna o numero de linhas na tabela de usuarios. */
    public int contarLinhas() {
        if (!elementoEstaPresente(TABELA_USUARIOS)) {
            return 0;
        }
        List<WebElement> linhas = driver.findElements(LINHAS_TABELA);
        return linhas.size();
    }

    /** Verifica se a mensagem "Nenhum usuario cadastrado" esta visivel. */
    public boolean mensagemSemRegistrosVisivel() {
        return elementoEstaPresente(SEM_REGISTROS);
    }

    /** Retorna o texto de uma celula especifica na tabela (linha, coluna baseadas em 0). */
    public String getTextoCelula(int linha, int coluna) {
        String cssSelector = String.format("#tabelaUsuarios tbody tr:nth-child(%d) td:nth-child(%d)", linha + 1, coluna + 1);
        return aguardarElementoVisivel(By.cssSelector(cssSelector)).getText();
    }

    /** Clica no botao "Editar" de um usuario na linha indicada (baseada em 0). */
    public UsuarioFormularioPage clicarEditar(int linha) {
        String cssSelector = String.format("#tabelaUsuarios tbody tr:nth-child(%d) .btn-editar", linha + 1);
        aguardarElementoClicavel(By.cssSelector(cssSelector)).click();
        return new UsuarioFormularioPage(driver);
    }

    /** Clica no botao "Excluir" de um usuario na linha indicada (baseada em 0). */
    public UsuarioListagemPage clicarExcluir(int linha) {
        String cssSelector = String.format("#tabelaUsuarios tbody tr:nth-child(%d) .btn-excluir", linha + 1);
        aguardarElementoClicavel(By.cssSelector(cssSelector)).click();
        return this;
    }

    /** Confirma o dialogo de confirmacao do navegador (alert). */
    public UsuarioListagemPage confirmarExclusao() {
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // Alert pode ja ter sido processado
        }
        return this;
    }

    /** Cancela o dialogo de confirmacao do navegador (alert). */
    public UsuarioListagemPage cancelarExclusao() {
        try {
            driver.switchTo().alert().dismiss();
        } catch (Exception e) {
            // Alert pode ja ter sido processado
        }
        return this;
    }

    /** Verifica se a tabela de usuarios esta visivel. */
    public boolean tabelaEstaVisivel() {
        return elementoEstaPresente(TABELA_USUARIOS);
    }
}
