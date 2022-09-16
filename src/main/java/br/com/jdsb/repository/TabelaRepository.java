package br.com.jdsb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jdsb.domain.Tabela;

public interface TabelaRepository extends JpaRepository<Tabela, Integer> {
	
	Tabela findByNmTabela(String nmTabela);
	List<Tabela> findByDsStatusTrigger(String dsStatusTrigger);

}
