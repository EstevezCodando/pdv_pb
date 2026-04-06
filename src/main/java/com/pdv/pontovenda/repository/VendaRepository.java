package com.pdv.pontovenda.repository;

import com.pdv.pontovenda.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("select coalesce(sum(v.valorTotal), 0) from Venda v")
    BigDecimal somarValorTotal();
}
