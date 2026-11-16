package br.edu.mackenzie.estudiobdd.repository;

import java.util.List;
import java.util.Optional;

import br.edu.mackenzie.estudiobdd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByNomeAsc();
    Optional<Usuario> findByEmail(String email);
}
