package de.borstelmann.doorbell.server.domain.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity(name = "users")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String oAuthId;

    @OneToMany(mappedBy = "user")
    private List<DoorbellDevice> doorbellDevices;

}
