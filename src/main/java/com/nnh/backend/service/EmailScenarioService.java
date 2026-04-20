package com.nnh.backend.service;

import com.nnh.backend.persistence.entity.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * One method per email scenario. Add new scenarios here as needed.
 * EmailService handles the actual sending; this layer owns the content.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailScenarioService {

    private final EmailService emailService;
    private final EmploymentContractGenerator contractGenerator;

    @Value("${app.mail.cc}")
    private String adminCc;

    // ── Scenario: New Employee Welcome ────────────────────────────────────────

    public void sendNewEmployeeWelcome(Employee employee, String tempPassword) {
        String subject = "Welcome to Nanda Nursing Home — Your Account Details";

        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;color:#1a1a1a">
              <div style="background:#7c1d3f;padding:24px 32px;border-radius:8px 8px 0 0">
                <h1 style="color:#fff;margin:0;font-size:20px">Nanda Nursing Home</h1>
                <p style="color:#f5c6d7;margin:4px 0 0;font-size:13px">Women's Health &amp; Gynecology, Dehradun</p>
              </div>
              <div style="padding:32px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 8px 8px">
                <h2 style="font-size:18px;margin:0 0 8px">Welcome aboard, %s!</h2>
                <p style="color:#4b5563;line-height:1.6">
                  We are delighted to welcome you to the Nanda Nursing Home family. Your account has been
                  created and you can log in to the staff portal using the credentials below.
                </p>
                <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:20px;margin:20px 0">
                  <p style="margin:0 0 8px"><strong>Employee ID:</strong> %s</p>
                  <p style="margin:0 0 8px"><strong>Email:</strong> %s</p>
                  <p style="margin:0"><strong>Temporary Password:</strong> <code style="background:#fee2e2;padding:2px 6px;border-radius:4px;font-size:14px">%s</code></p>
                </div>
                <p style="color:#4b5563;line-height:1.6">
                  Please log in and change your password immediately. Your employment contract is attached
                  to this email for your records.
                </p>
                <a href="https://nandanursinghome.in/login"
                   style="display:inline-block;background:#7c1d3f;color:#fff;text-decoration:none;padding:12px 24px;border-radius:6px;font-weight:bold;margin-top:8px">
                  Log In to Staff Portal
                </a>
                <hr style="border:none;border-top:1px solid #e5e7eb;margin:32px 0">
                <p style="color:#9ca3af;font-size:12px;margin:0">
                  Nanda Nursing Home · A5, Jagriti Vihar Lane 2, Badripur, Dehradun, Uttarakhand 248005<br>
                  This is an automated message. Please do not reply to this email.
                </p>
              </div>
            </div>
            """.formatted(
                employee.getName().split(" ")[0],
                employee.getId(),
                employee.getEmail(),
                tempPassword
        );

        byte[] contract = contractGenerator.generate(employee);
        emailService.send(
                employee.getEmail(),
                adminCc,
                subject,
                html,
                new EmailService.Attachment(
                        "Employment-Contract-" + employee.getId() + ".pdf",
                        contract,
                        "application/pdf"
                )
        );
    }

    // ── Add new scenarios below ───────────────────────────────────────────────
    // Examples (implement when needed):
    //   sendLeaveApproved(EmployeeLeave leave)
    //   sendLeaveRejected(EmployeeLeave leave)
    //   sendAppointmentRescheduled(Appointment appt, LocalDate newDate)
}
