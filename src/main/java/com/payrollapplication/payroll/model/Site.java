package com.payrollapplication.payroll.model;

import org.hibernate.annotations.Check;

import javax.persistence.*;

@Entity
@Table(name = "sites", indexes = {
        @Index(name = "idx_sites_name", columnList = "name"),
        @Index(name = "idx_sites_location", columnList = "location")
})
@Check(constraints = "name IS NOT NULL AND name <> ''")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    public Site() {
    }

    public Site(String name, String location, ActiveStatus status) {
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ActiveStatus getStatus() {
        return status;
    }

    public void setStatus(ActiveStatus status) {
        this.status = status;
    }
}
