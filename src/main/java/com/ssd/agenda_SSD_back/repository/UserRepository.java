package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
