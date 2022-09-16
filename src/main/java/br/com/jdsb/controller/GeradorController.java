package br.com.jdsb.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.jdsb.domain.Coluna;
import br.com.jdsb.domain.Tabela;
import br.com.jdsb.service.AuditorServices;

@Configuration
public class GeradorController {

	@Autowired
	private AuditorServices services;
	
	@Autowired
	private TabelaController tabelas;
	
	@Bean
	public void processaAuditoria() {
		services.getTabela().deleteAll();
		tabelas.salvaTabela();
		
		Tabela tabelaPrincipal = services.getTabela().findByNmTabela("AUDIT_SUPRIMENTOS");
		
		List<Tabela> tabelasSecundarias = services.getTabela().findAll();
		
			for(Tabela tabela:tabelasSecundarias) {
				if(tabela.getNmTabela().equals("AUDIT_SUPRIMENTOS")) {
					continue;
				}
				StringBuilder trigger = new StringBuilder();
				trigger.append("CREATE OR REPLACE TRIGGER DBAMV.").append(normalizaNmTrigger(tabela.getNmTabela()));
				trigger.append("\n");
				trigger.append("AFTER INSERT OR DELETE OR UPDATE OF");
				trigger.append(" ");
				List<Coluna> colunas = new ArrayList<>();
				for(Coluna coluna:tabelaPrincipal.getColunas()) {
					for(Coluna colunaTabelaFilha:tabela.getColunas()) {
						if(coluna.getNmColuna().equals(colunaTabelaFilha.getNmColuna())) {
							colunas.add(colunaTabelaFilha);
						}
					}
				}
				for(Coluna coluna:colunas) {
					trigger.append(coluna.getNmColuna());
					if(colunas.indexOf(coluna)!=colunas.size()-1) {
						trigger.append(",");
					}else {
						trigger.append(" ON ").append("DBAMV.").append(tabela.getNmTabela());
					}
				}
				trigger.append("\n");
				trigger.append("REFERENCING NEW AS NEW OLD AS OLD");
				trigger.append("\n");
				trigger.append("FOR EACH ROW");
				trigger.append("\n");
				trigger.append("DECLARE");
				trigger.append("\n");
				trigger.append("V_TP_OPERACAO_PROCESSO VARCHAR2(30);");
				trigger.append("\n");
				trigger.append("BEGIN");
				trigger.append("\n");
				trigger.append("IF INSERTING THEN");
				trigger.append("\n");
				trigger.append("V_TP_OPERACAO_PROCESSO := 'INSERT';");
				trigger.append("\n");
				trigger.append("END IF;");
				trigger.append("\n");
				trigger.append("IF UPDATING THEN");
				trigger.append("\n");
				trigger.append("V_TP_OPERACAO_PROCESSO := 'UPDATE';");
				trigger.append("\n");
				trigger.append("END IF;");
				trigger.append("\n");
				trigger.append("IF DELETING THEN");
				trigger.append("\n");
				trigger.append("V_TP_OPERACAO_PROCESSO := 'DELETE';");
				trigger.append("\n");
				trigger.append("END IF;");
				trigger.append("\n");
				trigger.append(processaInsert(colunas,tabela.getNmTabela()));
				trigger.append("\n");
				trigger.append(processaValues(colunas, tabela.getNmTabela()));
				
				trigger.append("\n");
				trigger.append("END ").append(normalizaNmTrigger(tabela.getNmTabela())).append(";");
				System.out.println(trigger.toString());
				
				services.getOracle().compilaObjeto(trigger.toString());
				
				tabela.setDsTriggerGerada(trigger.toString());
				tabela.setDsStatusTrigger(services.getOracle().retornaStatusProcedure(normalizaNmTrigger(tabela.getNmTabela())));
				tabela.setNmTrigger(normalizaNmTrigger(tabela.getNmTabela()));
				services.getTabela().save(tabela);
		  }
			
		List<Tabela> tabs = services.getTabela().findByDsStatusTrigger("VALID");
		gerarArquivo(tabs);
			
	}
	
	public String normalizaNmTrigger(String nmTabela) {
		nmTabela = "TRG_SUPRI_".concat(nmTabela);
		String retorno = "";
		int tamanhoNomeTabela = nmTabela.length();
		if(tamanhoNomeTabela>=30) {
			retorno = nmTabela.substring(0,29);
		}else {
			retorno = nmTabela;
		}
		return retorno;
	}
	
	public String normalizaNmColuna(String nmColuna) {
		String retorno = nmColuna;
		nmColuna = nmColuna.concat("_OLD");
		int tamanhoNomeColuna = nmColuna.length();
		if(tamanhoNomeColuna>=30) {
			int remove = tamanhoNomeColuna - 30;
			retorno = retorno.substring(0,retorno.length()-remove).concat("_OLD");
		}else {
			retorno = nmColuna;
		}
		return retorno;
	}
	
	
	public StringBuilder processaInsert(List<Coluna> colunas,String nmTabela) {
		StringBuilder insert = new StringBuilder();
		insert.append("INSERT INTO DBAMV.AUDIT_SUPRIMENTOS").append("\n");
		insert.append("(").append("\n");
		insert.append("CD_AUDIT_SUPRIMENTOS,").append("\n");
		insert.append("NM_OBJETO,").append("\n");
		insert.append("DT_AUDIT_SUPRIMENTOS,").append("\n");
		insert.append("TP_OPERACAO_PROCESSO,").append("\n");
		insert.append("DS_USUARIO_PROCESSO,").append("\n");
		insert.append("DS_USUARIO_LOGADO,").append("\n");
		insert.append("DS_OPERACAO_REALIZADA,").append("\n");
		insert.append("DS_TELA_PROCESSO,").append("\n");
		insert.append("DS_HOST_PROCESSO,").append("\n");
		insert.append("TP_OBJETO,").append("\n");
		insert.append("DS_COMANDO,").append("\n");
		insert.append("CD_SID,").append("\n");
		//insert.append("CD_STATUS_PROCESSO,").append("\n");
		insert.append("SN_PROCESSADO,").append("\n");
		
		if(nmTabela.equals("MVTO_ESTOQUE") || nmTabela.equals("SOLSAI_PRO")  || nmTabela.equals("CONTAGEM")) {
			insert.append("TP_MOVIMENTO,").append("\n");
			insert.append("TP_MOVIMENTO_OLD,").append("\n");
			
			insert.append("DT_MOVIMENTO,").append("\n");
			insert.append("DT_MOVIMENTO_OLD,").append("\n");
			
			
			insert.append("HR_MOVIMENTO,").append("\n");
			insert.append("HR_MOVIMENTO_OLD,").append("\n");
		}
		
		if(nmTabela.equals("LOT_PRO") ||
		   nmTabela.equals("EST_PRO") ||
		   nmTabela.equals("ITCONTAGEM") ||
		   nmTabela.equals("ITMVTO_ESTOQUE") ||
		   nmTabela.equals("ITMVTO_KIT_PRODUZIDO") ||
		   nmTabela.equals("ITENT_PRO") ||
		   nmTabela.equals("ITLOT_ENT") ||
		   nmTabela.equals("PRODUTO_FRACIONAMENTO") ||
		   nmTabela.equals("COPIA_ESTOQUE") ||
		   nmTabela.equals("ITEM_CONTAGEM_USUARIO") ||
		   nmTabela.equals("ITDEV_FOR")
		   
		   ) {
				insert.append("QT_ITEM,").append("\n");
				insert.append("QT_ITEM_OLD,").append("\n");
		    }
		
		List<String> primarias = retornaDePara(nmTabela);
		
		for(Coluna coluna:colunas) {
			if(colunas.indexOf(coluna)!=colunas.size()-1) {
				insert.append(coluna.getNmColuna()).append(",").append("\n");
				if(excluiColunaOld(coluna)) 
				   insert.append(normalizaNmColuna(coluna.getNmColuna())).append(",").append("\n");
			}else {
				insert.append(coluna.getNmColuna()).append(",").append("\n");
				if(!primarias.isEmpty()) {
					if(excluiColunaOld(coluna)) 
				    insert.append(normalizaNmColuna(coluna.getNmColuna())).append(",").append("\n");
				}else {
					if(excluiColunaOld(coluna)) 
					insert.append(coluna.getNmColuna()).append("_OLD").append("\n");
				}
			}
		}
		
		if(!primarias.isEmpty()) {
			if(primarias.size()==2) {
				if(nmTabela.equals("MVTO_KIT_PRODUZIDO")) {
					insert.append("CD_MOVIMENTO").append(",").append("\n");
					insert.append("DSP_CODIGO_DE_BARRAS").append("").append("\n");
					}else {
						insert.append("CD_MOVIMENTO").append(",").append("\n");
						insert.append("CD_ITMOVIMENTO").append("").append("\n");
					}
			}else if(primarias.size()==1)  {
				insert.append("CD_MOVIMENTO").append("").append("\n");
			}
		}
		insert.append(")");
		return insert;
	}
	
	public StringBuilder processaValues(List<Coluna> colunas,String nmTabela) {
		StringBuilder values = new StringBuilder();
		values.append("VALUES").append("\n");
		values.append("(").append("\n");
		values.append("DBAMV.SEQ_AUDIT_SUPRIMENTOS.NEXTVAL,").append("\n");
		values.append("'").append(nmTabela).append("',").append("\n");
		values.append("SYSDATE,").append("\n");
		values.append("V_TP_OPERACAO_PROCESSO,").append("\n");
		values.append("SUBSTR(SYS_CONTEXT('USERENV', 'OS_USER'), 1, 2000),").append("\n");
		values.append("SUBSTR(SYS_CONTEXT('USERENV','CURRENT_USER'), 1, 2000),").append("\n");
		values.append("DBMS_UTILITY.FORMAT_CALL_STACK,").append("\n");
		values.append("SUBSTR(SYS_CONTEXT('USERENV', 'MODULE'), 1, 2000),").append("\n");
		values.append("SUBSTR(SYS_CONTEXT('USERENV', 'HOST'), 1, 2000),").append("\n");
		values.append("'TABLE',").append("\n");
		values.append("'DML',").append("\n");
		values.append("SYS_CONTEXT('USERENV', 'SID'),").append("\n");
		values.append("'I',").append("\n");
		//values.append("'N',").append("\n");
		if(nmTabela.equals("MVTO_ESTOQUE")) {
			values.append(":NEW.TP_MVTO_ESTOQUE,").append("\n");
			values.append(":OLD.TP_MVTO_ESTOQUE,").append("\n");
			
			values.append(":NEW.DT_MVTO_ESTOQUE,").append("\n");
			values.append(":OLD.DT_MVTO_ESTOQUE,").append("\n");
			
			values.append(":NEW.HR_MVTO_ESTOQUE,").append("\n");
			values.append(":OLD.HR_MVTO_ESTOQUE,").append("\n");
			
		}else if (nmTabela.equals("SOLSAI_PRO")) {
			values.append(":NEW.TP_SOLSAI_PRO,").append("\n");
			values.append(":OLD.TP_SOLSAI_PRO,").append("\n");
			
			values.append(":NEW.DT_SOLSAI_PRO,").append("\n");
			values.append(":OLD.DT_SOLSAI_PRO,").append("\n");
			
			values.append(":NEW.HR_SOLSAI_PRO,").append("\n");
			values.append(":OLD.HR_SOLSAI_PRO,").append("\n");
		} else if (nmTabela.equals("CONTAGEM")) {
			
			values.append(":NEW.TP_CONTAGEM,").append("\n");
			values.append(":OLD.TP_CONTAGEM,").append("\n");
			
			values.append(":NEW.DT_GERACAO,").append("\n");
			values.append(":OLD.DT_GERACAO,").append("\n");
			
			values.append(":NEW.HR_GERACAO,").append("\n");
			values.append(":OLD.HR_GERACAO,").append("\n");
		}
		
		if(nmTabela.equals("LOT_PRO") || nmTabela.equals("EST_PRO")){
			values.append(":NEW.QT_ESTOQUE_ATUAL,").append("\n");
			values.append(":OLD.QT_ESTOQUE_ATUAL,").append("\n");
	    }else if (nmTabela.equals("ITMVTO_ESTOQUE")  || nmTabela.equals("ITMVTO_KIT_PRODUZIDO") ) {
	    	values.append(":NEW.QT_MOVIMENTACAO,").append("\n");
			values.append(":OLD.QT_MOVIMENTACAO,").append("\n");
	    }else if (nmTabela.equals("ITCONTAGEM")) {
	    	values.append(":NEW.QT_ESTOQUE,").append("\n");
			values.append(":OLD.QT_ESTOQUE,").append("\n");
	    	
	    }else if (nmTabela.equals("ITEM_CONTAGEM_USUARIO")) {
	    	values.append(":NEW.QT_ESTOQUE,").append("\n");
			values.append(":OLD.QT_ESTOQUE,").append("\n");
	    	
	    }
	    else if (nmTabela.equals("COPIA_ESTOQUE")) {
	    	values.append(":NEW.QT_ESTOQUE,").append("\n");
			values.append(":OLD.QT_ESTOQUE,").append("\n");
	    	
	    }
	    else if (nmTabela.equals("ITENT_PRO")) {
	    	values.append(":NEW.QT_ENTRADA,").append("\n");
			values.append(":OLD.QT_ENTRADA,").append("\n");
	    	
	    }else if (nmTabela.equals("ITDEV_FOR")) {
	    	values.append(":NEW.QT_DEVOLVIDA,").append("\n");
			values.append(":OLD.QT_DEVOLVIDA,").append("\n");
	    	
	    }else if (nmTabela.equals("PRODUTO_FRACIONAMENTO")) {
	    	values.append(":NEW.QT_PROD_FRACIONADO,").append("\n");
			values.append(":OLD.QT_PROD_FRACIONADO,").append("\n");
	    	
	    }else if (nmTabela.equals("ITLOT_ENT")) {
	    	values.append(":NEW.QT_ENT_PRO,").append("\n");
			values.append(":OLD.QT_ENT_PRO,").append("\n");
	    	
	    }
		
		
		List<String> primarias = retornaDePara(nmTabela);
		for(Coluna coluna:colunas) {
			if(colunas.indexOf(coluna)!=colunas.size()-1) {
				values.append(":NEW.").append(coluna.getNmColuna()).append(",").append("\n");
				if(excluiColunaOld(coluna)) 
				values.append(":OLD.").append(coluna.getNmColuna()).append(",").append("\n");
			}else {
				values.append(":NEW.").append(coluna.getNmColuna()).append(",").append("\n");
				if(!primarias.isEmpty()) {
					if(excluiColunaOld(coluna)) 
					values.append(":OLD.").append(coluna.getNmColuna()).append(",").append("\n");
				}else {
					if(excluiColunaOld(coluna)) 
				    values.append(":OLD.").append(coluna.getNmColuna()).append("\n");
				}
			}
		}
		
		if(!primarias.isEmpty()) {
			for(String coluna:primarias) {
				if(primarias.indexOf(coluna)!=primarias.size()-1) {
					values.append(":NEW.").append(coluna).append(",").append("\n");
				}else {
					values.append(":NEW.").append(coluna).append("").append("\n");
				}
			}
		}
		values.append(");");
		return values;
	}
	
	boolean excluiColunaOld(Coluna coluna) {
		if(!coluna.getNmColuna().equals("CD_INVENTARIO") 
				   && !coluna.getNmColuna().equals("DT_ENTRADA")
				   && !coluna.getNmColuna().equals("CD_MULTI_EMPRESA")) 
			return true;
		else 
			return false;
	}
	
	public List<String> retornaDePara(String nmTabela) {
		List<String> listaDePara = new ArrayList<>();
		switch (nmTabela) {
		case "MVTO_ESTOQUE":
			 listaDePara.add("CD_MVTO_ESTOQUE");
			break;
		case "ITMVTO_ESTOQUE":
			 listaDePara.add("CD_MVTO_ESTOQUE");
			 listaDePara.add("CD_ITMVTO_ESTOQUE");
			break;
		case "ENT_PRO":	
			 listaDePara.add("CD_ENT_PRO");
			break;
		case "ITENT_PRO":	
			 listaDePara.add("CD_ENT_PRO");
			 listaDePara.add("CD_ITENT_PRO");
			break;	
		case "CONTAGEM":	
			 listaDePara.add("CD_CONTAGEM");
			break;	
		case "ITCONTAGEM":	
			 listaDePara.add("CD_CONTAGEM");
			 listaDePara.add("CD_LINHA");
			break;	
		case "DEV_FOR":	
			 listaDePara.add("CD_DEVOLUCAO");
			break;
		case "ITDEV_FOR":	
			 listaDePara.add("CD_DEVOLUCAO");
			 listaDePara.add("CD_ITDEV_FOR");
			break;	
		case "MVTO_KIT_PRODUZIDO":	
			 listaDePara.add("CD_MVTO_ESTOQUE");
			 listaDePara.add("DSP_CD_BARRAS");
			 
			break;
		case "ITMVTO_KIT_PRODUZIDO":	
			 listaDePara.add("CD_MVTO_ESTOQUE");
			 listaDePara.add("CD_ITMVTO_ESTOQUE");
			break;	
		case "SOLSAI_PRO":	
			 listaDePara.add("CD_SOLSAI_PRO");
			break;
		case "ITSOLSAI_PRO":	
			 listaDePara.add("CD_SOLSAI_PRO");
			 listaDePara.add("CD_ITSOLSAI_PRO");			 
			break;	
		case "ITLOT_ENT":	
			 listaDePara.add("CD_ITENT_PRO");
			 listaDePara.add("CD_ITLOT_ENT");			 
			break;	
		case "ITCONTAGEM_DA_ABERTURA":	
			 listaDePara.add("CD_CONTAGEM");
			 listaDePara.add("CD_SEQUENCIAL");			 
			break;	
		case "ITEM_CONTAGEM_USUARIO":	
			 listaDePara.add("CD_CONTAGEM");
			 listaDePara.add("CD_LINHA");			 
			break;	
		case "COPIA_ESTOQUE":	
			 listaDePara.add("CD_CONTAGEM");
			 listaDePara.add("CD_COPIA_ESTOQUE");			 
			break;		

		default:
			break;
		}
		return listaDePara;
	}
	
	private void gerarArquivo(List<Tabela> tabelas) {
		String caminho = "C:\\extrator\\auditoria\\";
		for(Tabela tabela:tabelas) {
			try {
				String caminhoProcedures = caminho.concat("\\procedures\\");
				String caminhoTriggers= caminho.concat("\\triggers\\");
				//Files.writeString(new File(caminhoProcedures.concat(trigger.getNmProcedure().concat(".SQL"))).toPath(), trigger.getDsProcedureGerada(), StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE);
				Files.writeString(new File(caminhoTriggers.concat("DBAMV_TRIGGER_").concat(tabela.getNmTrigger().concat(".SQL"))).toPath(), tabela.getDsTriggerGerada(), StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
