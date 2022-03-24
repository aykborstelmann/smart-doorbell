package de.borstelmann.doorbell.server.domain.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DoorbellDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    private String name;

    @Builder.Default
    private Boolean isConnected = false;

    @Builder.Default
    private Boolean isOpened = false;

    private OffsetDateTime lastNotified;
    
}
