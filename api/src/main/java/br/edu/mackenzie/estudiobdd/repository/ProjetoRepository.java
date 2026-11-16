package br.edu.mackenzie.estudiobdd.repository;

import java.util.List;
import java.util.Optional;

import br.edu.mackenzie.estudiobdd.model.Projeto;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    List<Projeto> findAllByOrderByNomeAsc();
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Projeto p where p.id = :id")
    Optional<Projeto> buscarParaAlterar(@Param("id") Long id);
}
