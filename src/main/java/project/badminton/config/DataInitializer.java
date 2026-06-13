package project.badminton.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.badminton.court.Court;
import project.badminton.court.CourtRepository;
import project.badminton.timeslot.TimeSlot;
import project.badminton.timeslot.TimeSlotRepository;
import project.badminton.user.Role;
import project.badminton.user.User;
import project.badminton.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository,
            CourtRepository courtRepository,
            TimeSlotRepository timeSlotRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            User admin = createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "admin",
                    "admin@badminton.local",
                    "Quản trị viên hệ thống",
                    Set.of(Role.ROLE_ADMIN)
            );
            User manager = createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "manager",
                    "manager@badminton.local",
                    "Quản lý sân cầu lông",
                    Set.of(Role.ROLE_MANAGER)
            );
            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "customer",
                    "customer@badminton.local",
                    "Khách hàng mẫu",
                    Set.of(Role.ROLE_CUSTOMER)
            );

            if (timeSlotRepository.count() == 0) {
                createTimeSlot(timeSlotRepository, 7, 9);
                createTimeSlot(timeSlotRepository, 9, 11);
                createTimeSlot(timeSlotRepository, 14, 16);
                createTimeSlot(timeSlotRepository, 18, 20);
                createTimeSlot(timeSlotRepository, 20, 22);
            }

            if (courtRepository.count() == 0) {
                createCourt(courtRepository, manager, "Sân A1", "Quận 1, Thành phố Hồ Chí Minh", "Sân trong nhà với hệ thống chiếu sáng tiêu chuẩn", "120000");
                createCourt(courtRepository, manager, "Sân A2", "Quận 1, Thành phố Hồ Chí Minh", "Sân phù hợp với học sinh, sinh viên", "100000");
            }
        };
    }

    private User createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String fullName,
            Set<Role> roles
    ) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone("0900000000");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRoles(roles);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }

    private void createTimeSlot(TimeSlotRepository repository, int startHour, int endHour) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(LocalTime.of(startHour, 0));
        timeSlot.setEndTime(LocalTime.of(endHour, 0));
        timeSlot.setActive(true);
        repository.save(timeSlot);
    }

    private void createCourt(CourtRepository repository, User manager, String name, String address, String description, String price) {
        Court court = new Court();
        court.setName(name);
        court.setAddress(address);
        court.setDescription(description);
        court.setHourlyPrice(new BigDecimal(price));
        court.setManager(manager);
        court.setActive(true);
        repository.save(court);
    }
}
