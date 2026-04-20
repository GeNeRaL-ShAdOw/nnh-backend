package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    List<EmployeeLeave> findAllByOrderByCreatedAtDesc();
    List<EmployeeLeave> findByEmployee_IdOrderByCreatedAtDesc(String employeeId);
}
