package de.borstelmann.doorbell.server.domain.model;

import lombok.*;

import javax.persistence.*;
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

    @OneToOne
    private DoorbellBuzzer doorbellBuzzer;

    @OneToOne
    private DoorbellNotifier doorbellNotifier;
    
}
