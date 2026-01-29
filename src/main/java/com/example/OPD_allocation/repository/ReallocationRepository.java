package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.Reallocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReallocationRepository extends JpaRepository<Reallocation, Long> {
    List<Reallocation> findByTokenId(Long tokenId);
}

