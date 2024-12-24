package com.example.auth_spring.service;

import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.Status;
import com.example.auth_spring.model.enums.Weekday;
import com.example.auth_spring.repository.BookedTimeslotRepo;
import com.example.auth_spring.repository.ExecutorRepo;
import com.example.auth_spring.repository.ExecutorScheduleRepo;
import com.example.auth_spring.repository.ProvidedServiceRepo;
import com.example.auth_spring.utils.TimeInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookedTimeslotsService {

    @Autowired
    private final BookedTimeslotRepo bookedTimeslotsRepository;
    @Autowired
    private ProvidedServiceRepo providedServiceRepo;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private ExecutorRepo executorRepo;
    @Autowired
    private ExecutorScheduleRepo executorScheduleRepo;

    @Autowired
    public BookedTimeslotsService(BookedTimeslotRepo bookedTimeslotsRepository) {
        this.bookedTimeslotsRepository = bookedTimeslotsRepository;
    }

    public static Weekday getWeekday(String str) {
        return switch (str) {
            case "MONDAY" -> Weekday.mon;
            case "TUESDAY" -> Weekday.tue;
            case "WEDNESDAY" -> Weekday.wed;
            case "THURSDAY" -> Weekday.thu;
            case "FRIDAY" -> Weekday.fri;
            case "SATURDAY" -> Weekday.sat;
            case "SUNDAY" -> Weekday.sun;
            default -> null;
        };
    }

    @Transactional
    public void bookTimeslot(BookedTimeslot timeslot) {
        bookedTimeslotsRepository.save(timeslot);
    }


    public List<BookedTimeslot> findByUser(Integer userId) {
        return bookedTimeslotsRepository.findByUserId(userId);
    }


    public List<BookedTimeslot> findByServiceAndExecutorId(Integer serviceId, Integer executorId) {
        Optional<ProvidedService> providedService = providedServiceRepo.findById(serviceId);
        Optional<Executor> executor = executorRepo.findById(executorId);
        if (providedService.isEmpty() && executor.isEmpty()) {
            return List.of();
        }
        return bookedTimeslotsRepository.findByExecutorToServiceExecutorIdAndExecutorToServiceProvidedServiceId(executor.get().getId(), providedService.get().getId());
    }

    public void deleteTimeslot(Integer id) {
        bookedTimeslotsRepository.deleteById(id);
    }

    public List<BookedTimeslot> findByDateAndExecutorId(LocalDate date, Integer executorId) {
        return bookedTimeslotsRepository.findBookedTimeslotsByDateAndExecutorId(date, executorId);
    }


    private static LocalTime addAvailableSlots(LocalTime start, LocalTime end, int durationMinutes,
                                                   List<TimeInterval> availableIntervals) {
        while (start.plusMinutes(durationMinutes).isBefore(end) || start.plusMinutes(durationMinutes).equals(end)) {
            LocalTime slotEnd = start.plusMinutes(durationMinutes);
            availableIntervals.add(new TimeInterval(start, slotEnd));
            start = slotEnd;
        }
        return start;
    }

    public List<TimeInterval> getFreeTimeslots(LocalDate date, Integer executorId, Integer serviceId, Integer companyId) {
        List<TimeInterval> freeTimeslots = new ArrayList<>();
        Optional<ProvidedService> providedService_opt = providedServiceRepo.findById(serviceId);
        Optional<Executor> executor_opt = executorRepo.findById(executorId);
        Optional<ExecutorSchedule> schedule_opt = executorScheduleRepo.findByExecutorIdAndWeekday(executorId, getWeekday(date.getDayOfWeek().toString()));
        if (providedService_opt.isEmpty() || executor_opt.isEmpty() || schedule_opt.isEmpty()) {
            return List.of();
        }
        Executor executor = executor_opt.get();
        ProvidedService providedService = providedService_opt.get();
        ExecutorSchedule schedule = schedule_opt.get();
        List<BookedTimeslot> bookings = bookedTimeslotsRepository.findBookedTimeslotsByDateAndExecutorId(date, executorId);
        bookings.sort(Comparator.comparing(BookedTimeslot::getTimeBegin));
        int duration = providedService.getDuration();
        LocalTime currentStart = schedule.getTimeBegin();
        for (BookedTimeslot booking : bookings) {
            TimeInterval book = new TimeInterval(booking.getTimeBegin().toLocalTime(), booking.getTimeEnd().toLocalTime());
            if (currentStart.isBefore(book.getStart())) {
                currentStart = addAvailableSlots(currentStart, book.getStart(), duration, freeTimeslots);

            }
            currentStart =  book.getEnd().isAfter(currentStart) ? book.getEnd() : currentStart;

        }
        if (currentStart.isBefore(schedule.getTimeEnd())) {
            addAvailableSlots(currentStart, schedule.getTimeEnd(), duration, freeTimeslots);
        }
        return freeTimeslots;
    }
    public boolean is_belongs(User user, Integer id){

        Optional<BookedTimeslot> booking =  bookedTimeslotsRepository.findById(id);
        if (booking.isEmpty()) {
            return false;
        }
        if(booking.get().getUser()==user){
            return true;
        }
        return false;
    }
    public List<BookedTimeslot> findByStatusAndCompanyId(Status status, Integer company_id) {
        return bookedTimeslotsRepository.findByExecutorToServiceExecutorCompanyIdAndStatus(company_id, status);
    }
    public Optional<BookedTimeslot> findById(Integer id){
        return bookedTimeslotsRepository.findById(id);
    }
}

