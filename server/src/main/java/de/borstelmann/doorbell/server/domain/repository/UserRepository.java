package de.borstelmann.doorbell.server.domain.repository;

import de.borstelmann.doorbell.server.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}