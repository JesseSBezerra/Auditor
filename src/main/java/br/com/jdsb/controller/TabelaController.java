package br.com.jdsb.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.jdsb.domain.Coluna;
import br.com.jdsb.domain.Tabela;
import br.com.jdsb.service.AuditorServices;

@Controller
public class TabelaController {

	@Autowired
	private AuditorServices services;
	
	List<String> tabelas = new ArrayList<>();
	
	public void salvaTabela() {
		tabelas.add("ENT_PRO");
		tabelas.add("ITENT_PRO");
		tabelas.add("ITLOT_ENT");
		
		tabelas.add("MVTO_ESTOQUE");
		tabelas.add("ITMVTO_ESTOQUE");
		
		tabelas.add("CONTAGEM");
		tabelas.add("ITCONTAGEM");
		tabelas.add("ITCONTAGEM_DA_ABERTURA");
		tabelas.add("ITEM_CONTAGEM_USUARIO");
		tabelas.add("COPIA_ESTOQUE");
		
		
		tabelas.add("DEVFOR");
		tabelas.add("ITDEV_FOR");
		
		tabelas.add("MVTO_KIT_PRODUZIDO");
		tabelas.add("ITMVTO_KIT_PRODUZIDO");
		
		tabelas.add("PRODUTO");
		tabelas.add("ITMVTO_KIT_PRODUZIDO");
		
		tabelas.add("UNI_PRO");
		tabelas.add("PRODUTO_FRACIONAMENTO");
		
		tabelas.add("SOLSAI_PRO");
		tabelas.add("ITSOLSAI_PRO");
		
		tabelas.add("AUDIT_SUPRIMENTOS");
		
		tabelas.add("LOT_PRO");
		tabelas.add("EST_PRO");
		
		
		
		for(String tab:tabelas) {
			Tabela tabela = new Tabela();
			tabela.setNmTabela(tab);
			List<Coluna> colunas = services.getOracle().getColunas(tab);
			services.getColuna().saveAll(colunas);
			tabela.setColunas(colunas);
			services.getTabela().save(tabela);
		}
	}
}
