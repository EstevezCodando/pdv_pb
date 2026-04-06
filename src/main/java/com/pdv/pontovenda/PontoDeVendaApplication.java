package com.pdv.pontovenda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do sistema PDV - Ponto de Venda.
 * Inicializa o contexto Spring Boot com perfil H2 por padrao.
 */
@SpringBootApplication
public class PontoDeVendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PontoDeVendaApplication.class, args);
    }
}
