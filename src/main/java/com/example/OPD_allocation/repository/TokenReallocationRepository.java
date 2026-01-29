package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.ReallocationReason;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.entity.TokenReallocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenReallocationRepository extends JpaRepository<TokenReallocation, Long> {
    List<TokenReallocation> findByTokenOrderByReallocationTimeDesc(Token token);
    List<TokenReallocation> findByReason(ReallocationReason reason);
    List<TokenReallocation> findByReallocationTimeBetween(LocalDateTime start, LocalDateTime end);
    Long countByToken(Token token);
}
