package de.borstelmann.doorbell.server.domain.repository;

import de.borstelmann.doorbell.server.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from users u where u.oAuthId = ?1")
    Optional<User> findByOAuthId(String oAuthId);

}
