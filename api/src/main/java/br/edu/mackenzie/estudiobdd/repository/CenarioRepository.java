package br.edu.mackenzie.estudiobdd.repository;

import java.util.Optional;

import br.edu.mackenzie.estudiobdd.model.Cenario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CenarioRepository extends JpaRepository<Cenario, Long> {
    @Query("select c.historia.id from Cenario c where c.id = :id")
    Optional<Long> buscarHistoriaId(@Param("id") Long id);
}
