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
public class DoorbellNotifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, mappedBy = "doorbellNotifier")
    private DoorbellDevice doorbellDevice;

}
