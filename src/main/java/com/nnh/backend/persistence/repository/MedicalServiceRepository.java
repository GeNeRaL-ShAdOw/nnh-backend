package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, String> {

    List<MedicalService> findByActiveTrue();
}
