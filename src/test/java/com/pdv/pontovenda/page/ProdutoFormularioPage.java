package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object para o formulario de cadastro/edicao de produtos.
 * Encapsula a interacao com campos e botoes do formulario.
 */
public class ProdutoFormularioPage extends BasePage {

    private static final By CAMPO_NOME = By.id("nome");
    private static final By CAMPO_DESCRICAO = By.id("descricao");
    private static final By CAMPO_PRECO = By.id("preco");
    private static final By CAMPO_ESTOQUE = By.id("quantidadeEstoque");
    private static final By CAMPO_COD_BARRAS = By.id("codigoBarras");
    private static final By CAMPO_ATIVO = By.id("ativo");
    private static final By BTN_SALVAR = By.id("btnSalvar");
    private static final By BTN_CANCELAR = By.id("btnCancelar");
    private static final By ALERTA_ERRO_FORM = By.id("alertErroForm");
    private static final By FEEDBACK_INVALIDO = By.cssSelector(".invalid-feedback");

    public ProdutoFormularioPage(WebDriver driver) {
        super(driver);
    }

    public ProdutoFormularioPage preencherNome(String nome) {
        WebElement campo = aguardarElementoVisivel(CAMPO_NOME);
        campo.clear();
        campo.sendKeys(nome);
        return this;
    }

    public ProdutoFormularioPage preencherDescricao(String descricao) {
        WebElement campo = aguardarElementoVisivel(CAMPO_DESCRICAO);
        campo.clear();
        campo.sendKeys(descricao);
        return this;
    }

    public ProdutoFormularioPage preencherPreco(String preco) {
        WebElement campo = aguardarElementoVisivel(CAMPO_PRECO);
        campo.clear();
        campo.sendKeys(preco);
        return this;
    }

    public ProdutoFormularioPage preencherEstoque(String estoque) {
        WebElement campo = aguardarElementoVisivel(CAMPO_ESTOQUE);
        campo.clear();
        campo.sendKeys(estoque);
        return this;
    }

    public ProdutoFormularioPage preencherCodigoBarras(String codigo) {
        WebElement campo = aguardarElementoVisivel(CAMPO_COD_BARRAS);
        campo.clear();
        campo.sendKeys(codigo);
        return this;
    }

    public ProdutoFormularioPage definirAtivo(boolean ativo) {
        WebElement checkbox = aguardarElementoVisivel(CAMPO_ATIVO);
        if (checkbox.isSelected() != ativo) {
            checkbox.click();
        }
        return this;
    }

    /** Preenche todos os campos do formulario de produto. */
    public ProdutoFormularioPage preencherFormulario(String nome, String descricao, String preco,
                                                     String estoque, String codigoBarras, boolean ativo) {
        preencherNome(nome);
        preencherDescricao(descricao);
        preencherPreco(preco);
        preencherEstoque(estoque);
        preencherCodigoBarras(codigoBarras);
        definirAtivo(ativo);
        return this;
    }

    public ProdutoListagemPage clicarSalvar() {
        aguardarElementoClicavel(BTN_SALVAR).click();
        return new ProdutoListagemPage(driver);
    }

    public ProdutoFormularioPage clicarSalvarComErro() {
        aguardarElementoClicavel(BTN_SALVAR).click();
        return this;
    }

    public ProdutoListagemPage clicarCancelar() {
        aguardarElementoClicavel(BTN_CANCELAR).click();
        return new ProdutoListagemPage(driver);
    }

    public boolean alertaErroFormVisivel() {
        return elementoEstaPresente(ALERTA_ERRO_FORM);
    }

    public String getTextoAlertaErroForm() {
        return aguardarElementoVisivel(ALERTA_ERRO_FORM).getText();
    }

    public boolean existeFeedbackInvalido() {
        try {
            return !driver.findElements(FEEDBACK_INVALIDO).isEmpty()
                    && driver.findElements(FEEDBACK_INVALIDO).stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public String getValorNome() {
        return aguardarElementoVisivel(CAMPO_NOME).getAttribute("value");
    }

    public String getValorPreco() {
        return aguardarElementoVisivel(CAMPO_PRECO).getAttribute("value");
    }
}
