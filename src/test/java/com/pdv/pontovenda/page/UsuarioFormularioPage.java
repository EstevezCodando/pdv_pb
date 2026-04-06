package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Page Object para o formulario de cadastro/edicao de usuarios.
 * Encapsula a interacao com campos, selects, checkboxes e botoes do formulario.
 */
public class UsuarioFormularioPage extends BasePage {

    private static final By CAMPO_NOME = By.id("nome");
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_SENHA = By.id("senha");
    private static final By CAMPO_PERFIL = By.id("perfil");
    private static final By CAMPO_ATIVO = By.id("ativo");
    private static final By BTN_SALVAR = By.id("btnSalvar");
    private static final By BTN_CANCELAR = By.id("btnCancelar");
    private static final By ALERTA_ERRO_FORM = By.id("alertErroForm");
    private static final By FEEDBACK_INVALIDO = By.cssSelector(".invalid-feedback");

    public UsuarioFormularioPage(WebDriver driver) {
        super(driver);
    }

    /** Preenche o campo Nome. */
    public UsuarioFormularioPage preencherNome(String nome) {
        WebElement campo = aguardarElementoVisivel(CAMPO_NOME);
        campo.clear();
        campo.sendKeys(nome);
        return this;
    }

    /** Preenche o campo E-mail. */
    public UsuarioFormularioPage preencherEmail(String email) {
        WebElement campo = aguardarElementoVisivel(CAMPO_EMAIL);
        campo.clear();
        campo.sendKeys(email);
        return this;
    }

    /** Preenche o campo Senha. */
    public UsuarioFormularioPage preencherSenha(String senha) {
        WebElement campo = aguardarElementoVisivel(CAMPO_SENHA);
        campo.clear();
        campo.sendKeys(senha);
        return this;
    }

    /** Seleciona o perfil no dropdown. */
    public UsuarioFormularioPage selecionarPerfil(String perfil) {
        Select select = new Select(aguardarElementoVisivel(CAMPO_PERFIL));
        select.selectByVisibleText(perfil);
        return this;
    }

    /** Marca ou desmarca o checkbox "Ativo". */
    public UsuarioFormularioPage definirAtivo(boolean ativo) {
        WebElement checkbox = aguardarElementoVisivel(CAMPO_ATIVO);
        if (checkbox.isSelected() != ativo) {
            checkbox.click();
        }
        return this;
    }

    /** Preenche todos os campos do formulario de uma vez. */
    public UsuarioFormularioPage preencherFormulario(String nome, String email, String senha, String perfil, boolean ativo) {
        preencherNome(nome);
        preencherEmail(email);
        preencherSenha(senha);
        selecionarPerfil(perfil);
        definirAtivo(ativo);
        return this;
    }

    /** Clica no botao Salvar/Atualizar e retorna a pagina de listagem (em caso de sucesso). */
    public UsuarioListagemPage clicarSalvar() {
        aguardarElementoClicavel(BTN_SALVAR).click();
        return new UsuarioListagemPage(driver);
    }

    /** Clica no botao Salvar/Atualizar permanecendo na mesma pagina (em caso de erro). */
    public UsuarioFormularioPage clicarSalvarComErro() {
        aguardarElementoClicavel(BTN_SALVAR).click();
        return this;
    }

    /** Clica no botao Cancelar. */
    public UsuarioListagemPage clicarCancelar() {
        aguardarElementoClicavel(BTN_CANCELAR).click();
        return new UsuarioListagemPage(driver);
    }

    /** Verifica se existe mensagem de erro de regra de negocio no formulario. */
    public boolean alertaErroFormVisivel() {
        return elementoEstaPresente(ALERTA_ERRO_FORM);
    }

    /** Retorna o texto da mensagem de erro do formulario. */
    public String getTextoAlertaErroForm() {
        return aguardarElementoVisivel(ALERTA_ERRO_FORM).getText();
    }

    /** Verifica se existem campos com feedback de validacao. */
    public boolean existeFeedbackInvalido() {
        try {
            return !driver.findElements(FEEDBACK_INVALIDO).isEmpty()
                    && driver.findElements(FEEDBACK_INVALIDO).stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    /** Retorna o valor atual do campo Nome. */
    public String getValorNome() {
        return aguardarElementoVisivel(CAMPO_NOME).getAttribute("value");
    }

    /** Retorna o valor atual do campo E-mail. */
    public String getValorEmail() {
        return aguardarElementoVisivel(CAMPO_EMAIL).getAttribute("value");
    }
}
