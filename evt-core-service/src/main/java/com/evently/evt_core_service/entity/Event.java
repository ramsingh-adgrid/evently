package com.evently.evt_core_service.entity;

import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name ="events", indexes = {
    @Index(name = "idx_events_city", columnList = "city"),
    @Index(name = "idx_events_category", columnList = "category"),
    @Index(name = "idx_events_status", columnList = "status")
})
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
