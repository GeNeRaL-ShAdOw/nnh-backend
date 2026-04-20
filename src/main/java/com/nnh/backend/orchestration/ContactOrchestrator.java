package com.nnh.backend.orchestration;

import com.nnh.backend.dto.request.ContactRequest;
import com.nnh.backend.dto.response.ContactResponse;
import com.nnh.backend.persistence.entity.ContactMessage;
import com.nnh.backend.persistence.entity.ContactStatus;
import com.nnh.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates the contact message submission workflow.
 *
 * Step order:
 *  1. Map the inbound DTO to a domain entity.
 *  2. Persist via {@link ContactService}.
 *  3. (Stub) Notify clinic staff.
 *
 * Simple today, but this class is the right place to add auto-reply emails,
 * ticket creation in an external system, or spam filtering in the future.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContactOrchestrator {

    private final ContactService contactService;

    // ── Public workflow methods ───────────────────────────────────────────────

    public ContactResponse submit(ContactRequest req) {
        log.info("Submitting contact message from '{}'", req.getEmail());

        ContactMessage saved = contactService.create(
            ContactMessage.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .subject(req.getSubject())
                .message(req.getMessage())
                .status(ContactStatus.NEW)
                .build()
        );

        log.info("Contact message {} stored (NEW)", saved.getId());

        // Stub — plug in email notification to staff here when ready
        // notificationService.notifyStaff(saved);

        return toResponse(saved);
    }

    public ContactResponse getById(Long id) {
        return toResponse(contactService.getById(id));
    }

    public List<ContactResponse> getAll() {
        return contactService.getAll().stream().map(this::toResponse).toList();
    }

    public ContactResponse updateStatus(Long id, ContactStatus status) {
        return toResponse(contactService.updateStatus(id, status));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private ContactResponse toResponse(ContactMessage m) {
        return ContactResponse.builder()
            .id(m.getId())
            .name(m.getName())
            .email(m.getEmail())
            .phone(m.getPhone())
            .subject(m.getSubject())
            .message(m.getMessage())
            .status(m.getStatus().name())
            .createdAt(m.getCreatedAt())
            .build();
    }
}
