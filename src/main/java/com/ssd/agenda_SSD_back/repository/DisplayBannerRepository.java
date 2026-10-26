package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.DisplayBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisplayBannerRepository extends JpaRepository<DisplayBanner, Long> {

    // Só a versão, sem carregar os bytes da imagem — é o que o GET de
    // configurações (chamado pela TV a cada 30s) precisa.
    @Query("select b.version from DisplayBanner b where b.id = :id")
    Optional<Long> findVersionById(@Param("id") Long id);
}
