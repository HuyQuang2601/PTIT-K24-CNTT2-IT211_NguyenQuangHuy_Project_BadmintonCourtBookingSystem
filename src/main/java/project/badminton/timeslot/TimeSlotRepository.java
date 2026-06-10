package project.badminton.timeslot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByActiveTrueOrderByStartTimeAsc();
}
