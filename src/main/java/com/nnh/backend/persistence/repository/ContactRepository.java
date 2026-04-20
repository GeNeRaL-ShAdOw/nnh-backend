package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.ContactMessage;
import com.nnh.backend.persistence.entity.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByStatus(ContactStatus status);

    List<ContactMessage> findByEmail(String email);
}
