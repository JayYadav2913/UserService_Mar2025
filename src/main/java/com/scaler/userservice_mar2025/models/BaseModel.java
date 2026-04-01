package com.scaler.userservice_mar2025.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Where;
import java.util.Date;


@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "state = 'ACTIVE'")
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    private Date CreatedAt;
    @LastModifiedDate
    private Date lastModifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

    @PrePersist
    public void onCreate() {
        if (this.state == null) {
            this.state = State.ACTIVE;  // every new record starts as ACTIVE
        }
    }

}
