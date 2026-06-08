package com.evently.evt_core_service.entity;

import com.evently.evt_core_service.enums.Category;
import com.evently.evt_core_service.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name ="events")
@Getter
@Setter
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String organizerName;

    @Column(nullable = false , unique = true)
    private String organizerMobile;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

}
