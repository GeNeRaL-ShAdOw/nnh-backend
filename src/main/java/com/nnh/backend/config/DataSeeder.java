package com.nnh.backend.config;

import com.nnh.backend.persistence.entity.Doctor;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.entity.EmployeeRole;
import com.nnh.backend.persistence.entity.MedicalService;
import com.nnh.backend.persistence.repository.DoctorRepository;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import com.nnh.backend.persistence.repository.MedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds reference data (doctors, services) on startup if the tables are empty.
 *
 * IDs and content mirror the frontend's {@code src/data/index.ts} so that
 * dropdowns populated from the API match what the appointment form expects.
 *
 * This seeder is idempotent — running it repeatedly is safe.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final DoctorRepository doctorRepository;
    private final MedicalServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedDoctors();
        seedServices();
        seedEmployees();
    }

    // ── Employees ─────────────────────────────────────────────────────────────
    // Default passwords — CHANGE THESE after first login.
    //   care@nandanursinghome.in               → Care@NNH2025   (ADMIN – main admin account)
    //   drvaishnavipurohit@nandanursinghome.in → Admin@NNH2025  (ADMIN)

    private void seedEmployees() {
        if (employeeRepository.count() > 0) {
            log.debug("Employees already present, skipping seed.");
            return;
        }
        log.info("Seeding staff accounts...");

        employeeRepository.saveAll(List.of(
            Employee.builder()
                .id("NNHE-0000001")
                .name("Care Admin")
                .email("care@nandanursinghome.in")
                .passwordHash(passwordEncoder.encode("Care@NNH2025"))
                .role(EmployeeRole.ADMIN)
                .active(true)
                .mustChangePassword(false)
                .build(),
            Employee.builder()
                .id("NNHE-0000002")
                .name("Dr. Vaishnavi Purohit")
                .email("drvaishnavipurohit@nandanursinghome.in")
                .passwordHash(passwordEncoder.encode("Admin@NNH2025"))
                .role(EmployeeRole.ADMIN)
                .active(true)
                .mustChangePassword(false)
                .build()
        ));
        log.info("Seeded 2 admin accounts. Change default passwords after first login.");
    }

    // ── Doctors ───────────────────────────────────────────────────────────────

    private void seedDoctors() {
        if (doctorRepository.count() > 0) {
            log.debug("Doctors already present, skipping seed.");
            return;
        }
        log.info("Seeding doctor data...");

        Doctor doctor = Doctor.builder()
            .id("dr-purohit")
            .name("Dr. Vaishnavi Purohit")
            .title("MBBS, DGO, DNB")
            .specialty("Obstetrics, Gynecology & Fertility")
            .experience("16 Years")
            .imageUrl("")
            .qualifications(List.of(
                "MBBS – Maulana Azad Medical College",
                "DGO – PGIMER",
                "DNB Gynecology & Obstetrics"
            ))
            .availability("Mon – Sat, 9 AM – 7 PM")
            .active(true)
            .build();

        doctorRepository.save(doctor);
        log.info("Seeded doctor: {}", doctor.getName());
    }

    // ── Medical services ──────────────────────────────────────────────────────

    private void seedServices() {
        if (serviceRepository.count() > 0) {
            log.debug("Medical services already present, skipping seed.");
            return;
        }
        log.info("Seeding medical service data...");

        List<MedicalService> services = List.of(
            MedicalService.builder()
                .id("obstetrics")
                .title("Obstetrics & Prenatal Care")
                .shortDesc("Comprehensive pregnancy care from conception to delivery, ensuring safety for mother and baby.")
                .fullDesc("Our obstetrics team provides complete prenatal monitoring, high-risk pregnancy management, normal and cesarean deliveries, and postnatal care.")
                .icon("🤱")
                .color("from-pink-400 to-rose-500")
                .build(),
            MedicalService.builder()
                .id("gynecology")
                .title("Gynecology")
                .shortDesc("Expert diagnosis and treatment of all gynecological conditions across every life stage.")
                .fullDesc("Covering routine check-ups, menstrual disorders, PCOS, endometriosis, fibroids, and minimally invasive surgeries.")
                .icon("🌸")
                .color("from-fuchsia-400 to-purple-500")
                .build(),
            MedicalService.builder()
                .id("fertility")
                .title("Fertility & IVF")
                .shortDesc("Advanced assisted reproduction technologies to help you start the family of your dreams.")
                .fullDesc("IUI, IVF, egg freezing, embryo transfer, hormonal therapy, and fertility counselling by specialist reproductive endocrinologists.")
                .icon("✨")
                .color("from-teal-400 to-cyan-500")
                .build(),
            MedicalService.builder()
                .id("menopause")
                .title("Menopause Management")
                .shortDesc("Personalised hormone therapy and wellness plans to ease the transition gracefully.")
                .fullDesc("Hormone replacement therapy evaluation, osteoporosis screening, cardiovascular risk management, and lifestyle coaching.")
                .icon("🌿")
                .color("from-emerald-400 to-green-500")
                .build(),
            MedicalService.builder()
                .id("minimally-invasive")
                .title("Minimally Invasive Surgery")
                .shortDesc("Laparoscopic and hysteroscopic procedures for faster recovery and less discomfort.")
                .fullDesc("Laparoscopic myomectomy, ovarian cystectomy, hysterectomy, diagnostic hysteroscopy, and LEEP procedures.")
                .icon("🔬")
                .color("from-blue-400 to-indigo-500")
                .build(),
            MedicalService.builder()
                .id("cancer-screening")
                .title("Cancer Screening & Prevention")
                .shortDesc("Early detection saves lives — regular Pap smears, HPV tests, and mammograms.")
                .fullDesc("Cervical cancer screening, colposcopy, endometrial biopsy, ovarian cancer markers, and referral pathways for oncology.")
                .icon("🛡️")
                .color("from-orange-400 to-amber-500")
                .build()
        );

        serviceRepository.saveAll(services);
        log.info("Seeded {} medical services.", services.size());
    }
}
