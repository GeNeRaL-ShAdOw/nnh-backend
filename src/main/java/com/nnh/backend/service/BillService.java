package com.nnh.backend.service;

import com.nnh.backend.dto.request.BillRequest;
import com.nnh.backend.dto.response.BillItemResponse;
import com.nnh.backend.dto.response.BillResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import com.nnh.backend.persistence.entity.Bill;
import com.nnh.backend.persistence.entity.BillItem;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.repository.AppointmentRepository;
import com.nnh.backend.persistence.repository.BillRepository;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private final BillRepository billRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public BillResponse create(BillRequest req, String createdByEmail) {
        Employee employee = employeeRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + createdByEmail));

        Bill bill = Bill.builder()
                .patientName(req.getPatientName())
                .patientEmail(req.getPatientEmail())
                .patientPhone(req.getPatientPhone())
                .appointmentId(req.getAppointmentId())
                .createdBy(employee)
                .total(BigDecimal.ZERO)
                .build();

        List<BillItem> items = req.getItems().stream().map(ir -> {
            BigDecimal lineTotal = ir.getUnitPrice().multiply(BigDecimal.valueOf(ir.getQuantity()));
            return BillItem.builder()
                    .bill(bill)
                    .description(ir.getDescription())
                    .category(ir.getCategory())
                    .unitPrice(ir.getUnitPrice())
                    .quantity(ir.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
        }).toList();

        BigDecimal total = items.stream()
                .map(BillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        bill.setTotal(total);
        bill.getItems().addAll(items);

        Bill saved = billRepository.save(bill);
        log.info("Bill {} created for '{}' by {} (total: {})", saved.getId(), saved.getPatientName(), createdByEmail, total);

        if (req.getAppointmentId() != null) {
            appointmentRepository.findById(req.getAppointmentId()).ifPresent(appt -> {
                appt.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appt);
                log.info("Appointment {} marked COMPLETED after bill generation", appt.getId());
            });
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getAll() {
        return billRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        return toResponse(billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id)));
    }

    private BillResponse toResponse(Bill b) {
        List<BillItemResponse> itemResponses = b.getItems().stream().map(i -> BillItemResponse.builder()
                .id(i.getId())
                .description(i.getDescription())
                .category(i.getCategory())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .lineTotal(i.getLineTotal())
                .build()).toList();

        return BillResponse.builder()
                .id(b.getId())
                .patientName(b.getPatientName())
                .patientEmail(b.getPatientEmail())
                .patientPhone(b.getPatientPhone())
                .appointmentId(b.getAppointmentId())
                .createdById(b.getCreatedBy().getId())
                .createdByName(b.getCreatedBy().getName())
                .total(b.getTotal())
                .items(itemResponses)
                .createdAt(b.getCreatedAt())
                .build();
    }
}
