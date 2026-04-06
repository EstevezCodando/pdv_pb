package com.pdv.pontovenda.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object para as paginas de vendas (historico e formulario de nova venda).
 */
public class VendaPage extends BasePage {

    public VendaPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verifica se a pagina de historico de vendas carregou.
     */
    public boolean historicoCarregou() {
        String fonte = driver.getPageSource();
        return fonte.contains("Venda") || fonte.contains("venda") || fonte.contains("Historico");
    }

    /**
     * Verifica se o formulario de nova venda esta disponivel.
     */
    public boolean formularioCarregou() {
        String fonte = driver.getPageSource();
        return !driver.findElements(By.name("formaPagamento")).isEmpty()
                || fonte.contains("Forma de Pagamento")
                || fonte.contains("Nova Venda")
                || fonte.contains("nova");
    }
}
