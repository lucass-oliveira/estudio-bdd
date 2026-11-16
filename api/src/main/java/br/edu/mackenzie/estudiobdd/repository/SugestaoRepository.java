package br.edu.mackenzie.estudiobdd.repository;

import java.util.Optional;

import br.edu.mackenzie.estudiobdd.model.Sugestao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {
    @Query("select s.historia.id from Sugestao s where s.id = :id")
    Optional<Long> buscarHistoriaId(@Param("id") Long id);
}
