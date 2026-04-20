package com.nnh.backend.service;

import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.ContactMessage;
import com.nnh.backend.persistence.entity.ContactStatus;
import com.nnh.backend.persistence.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core domain service for inbound contact messages.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public ContactMessage create(ContactMessage message) {
        log.info("Persisting contact message from '{}'", message.getEmail());
        return contactRepository.save(message);
    }

    @Transactional(readOnly = true)
    public ContactMessage getById(Long id) {
        return contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> getAll() {
        return contactRepository.findAll();
    }

    @Transactional
    public ContactMessage updateStatus(Long id, ContactStatus newStatus) {
        ContactMessage message = getById(id);
        log.info("Contact message {}: {} → {}", id, message.getStatus(), newStatus);
        message.setStatus(newStatus);
        return contactRepository.save(message);
    }
}
