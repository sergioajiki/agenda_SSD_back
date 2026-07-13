package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // Busca usada pela tela de gestão de acessos (admin) — casa o termo tanto
    // no nome quanto no email, sem diferenciar maiúsculas/minúsculas.
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
