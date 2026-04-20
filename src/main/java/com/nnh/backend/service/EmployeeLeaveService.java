package com.nnh.backend.service;

import com.nnh.backend.dto.request.LeaveRequest;
import com.nnh.backend.dto.response.LeaveResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.EmployeeLeave;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.entity.LeaveStatus;
import com.nnh.backend.persistence.repository.EmployeeLeaveRepository;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeLeaveService {

    private final EmployeeLeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAll() {
        return leaveRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getApproved() {
        return leaveRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return leaveRepository.findByEmployee_IdOrderByCreatedAtDesc(employee.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public LeaveResponse create(LeaveRequest req, String email) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        EmployeeLeave leave = EmployeeLeave.builder()
                .employee(employee)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        log.info("Leave request from {} ({} to {})", email, req.getStartDate(), req.getEndDate());
        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public LeaveResponse approve(Long id, String adminEmail) {
        EmployeeLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
        Employee admin = employeeRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(admin);
        log.info("Leave {} approved by {}", id, adminEmail);
        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public LeaveResponse reject(Long id, String adminEmail) {
        EmployeeLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
        Employee admin = employeeRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(admin);
        log.info("Leave {} rejected by {}", id, adminEmail);
        return toResponse(leaveRepository.save(leave));
    }

    private LeaveResponse toResponse(EmployeeLeave l) {
        return LeaveResponse.builder()
                .id(l.getId())
                .employeeId(l.getEmployee().getId())
                .employeeName(l.getEmployee().getName())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .reason(l.getReason())
                .status(l.getStatus().name())
                .reviewedByName(l.getReviewedBy() != null ? l.getReviewedBy().getName() : null)
                .createdAt(l.getCreatedAt())
                .build();
    }
}
