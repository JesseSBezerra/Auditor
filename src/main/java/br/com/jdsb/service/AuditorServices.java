package br.com.jdsb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jdsb.repository.ColunaRepository;
import br.com.jdsb.repository.OracleRepository;
import br.com.jdsb.repository.TabelaRepository;
import lombok.Getter;

@Getter
@Service
public class AuditorServices {
	
	@Autowired
	private TabelaRepository tabela;
	
	@Autowired
	private ColunaRepository coluna;
	
	@Autowired
	private OracleRepository oracle;

}
