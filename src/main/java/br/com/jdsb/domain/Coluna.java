package br.com.jdsb.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity(name = "MD_COLUNA")
public class Coluna {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "NM_COLUNA")
	private String nmColuna;
	
	@Column(name = "TP_COLUNA")
	private String tpColuna;
	

}
