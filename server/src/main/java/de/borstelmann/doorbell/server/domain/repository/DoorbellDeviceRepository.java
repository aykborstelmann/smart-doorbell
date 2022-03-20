package de.borstelmann.doorbell.server.domain.repository;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoorbellDeviceRepository extends JpaRepository<DoorbellDevice, Long> {

}
