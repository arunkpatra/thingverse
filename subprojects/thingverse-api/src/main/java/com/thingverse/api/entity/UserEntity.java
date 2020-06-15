package com.thingverse.api.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Entity
@Table(name = "user_table")
public class UserEntity extends AbstractPersistable<Long> implements UserDetails {

    @Column(name = "user_name")
    private String username;

    @Column(name = "password")
    private String password;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private List<AuthorityEntity> authorityEntities = new ArrayList<>();

    public List<AuthorityEntity> getAuthorityEntities() {
        return authorityEntities;
    }

    public void setAuthorityEntities(List<AuthorityEntity> authorityEntities) {
        this.authorityEntities = authorityEntities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorityEntities.stream().map(a -> (GrantedAuthority) a::getAuthority).collect(toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
