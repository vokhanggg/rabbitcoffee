package com.rabbitcoffee.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rabbitcoffee.enumvalue.ERoleName;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(name="ROLE")
public class Role implements GrantedAuthority {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated( EnumType.STRING)
    @Column(name="name")
    ERoleName name;

    @Override
    public String getAuthority() {
        return name.name();
    }

    public void setName(ERoleName name) {
        this.name = name;
    }

    @JsonIgnore
    public ERoleName getName() {
        return name;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
