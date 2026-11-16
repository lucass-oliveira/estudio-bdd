package br.edu.mackenzie.estudiobdd.repository;

import java.util.Optional;

import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoriaRepository extends JpaRepository<HistoriaUsuario, Long> {
    boolean existsByProjetoIdAndTitulo(Long projetoId, String titulo);
    boolean existsByProjetoIdAndTituloAndIdNot(Long projetoId, String titulo, Long id);

    @Query("select h.projeto.id from HistoriaUsuario h where h.id = :id")
    Optional<Long> buscarProjetoId(@Param("id") Long id);
}
