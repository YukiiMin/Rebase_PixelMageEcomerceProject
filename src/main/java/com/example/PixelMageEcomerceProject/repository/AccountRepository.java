package com.example.PixelMageEcomerceProject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<Account> findByEmailIgnoreActive(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    org.springframework.data.domain.Page<Account> findAll(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT a FROM Account a WHERE lower(a.role.roleName) = lower(:roleName)")
    org.springframework.data.domain.Page<Account> findByRoleName(String roleName, org.springframework.data.domain.Pageable pageable);
}
