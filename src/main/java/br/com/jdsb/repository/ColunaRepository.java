package br.com.jdsb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jdsb.domain.Coluna;

public interface ColunaRepository extends JpaRepository<Coluna, Integer> {

}
