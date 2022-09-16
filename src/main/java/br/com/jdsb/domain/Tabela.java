package br.com.jdsb.domain;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import lombok.Data;

@Data
@Entity(name = "MD_TABELA")
public class Tabela {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "NM_TABELA")
	private String nmTabela;
	
	@ElementCollection
	@LazyCollection(LazyCollectionOption.FALSE)
	@CollectionTable(name = "COLUNAS_TABELA", joinColumns = @JoinColumn(name = "ID"))
	private List<Coluna> colunas;
	
	@Column(name = "DS_TRIGGER_GERADA")
	private String dsTriggerGerada;
	
	@Column(name = "DS_STATUS_TRIGGER")
	private String dsStatusTrigger;
	
	@Column(name = "NM_TRIGGER")
	private String nmTrigger;

}
