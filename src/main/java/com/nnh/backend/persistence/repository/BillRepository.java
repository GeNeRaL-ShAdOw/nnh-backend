package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByOrderByCreatedAtDesc();
}
