package com.example.auth_spring.service;


import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.Weekday;
import com.example.auth_spring.repository.*;
import com.example.auth_spring.utils.Pair;
import com.example.auth_spring.utils.TimeInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Service
public class ExecutorService {
    private final ExecutorRepo executorRepository;
    private final ExecutorToServiceRepo executorToServiceRepo;
    private final ExecutorScheduleRepo executorScheduleRepo;
    private final ProvidedServiceRepo providedServiceRepo;
    private final BookedTimeslotRepo bookedTimeslotRepo;

    @Autowired

    public ExecutorService(ExecutorRepo executorRepository, ExecutorToServiceRepo executorServiceRepository, ExecutorScheduleRepo executorScheduleRepo, ProvidedServiceRepo providedServiceRepo, BookedTimeslotRepo bookedTimeslotRepo) {
        this.executorRepository = executorRepository;
        this.executorToServiceRepo = executorServiceRepository;
        this.executorScheduleRepo = executorScheduleRepo;
        this.providedServiceRepo = providedServiceRepo;
        this.bookedTimeslotRepo = bookedTimeslotRepo;

    }

    public Executor saveExecutor(Executor executor) {
        return executorRepository.save(executor);
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

    public List<Pair<Executor, Integer>> findExecutorsByCompanyWithCount(Integer companyId) {
        List<Pair<Executor, Integer>> executorsWithServiceCount = new ArrayList<>();
        List<Executor> execs = executorRepository.findByCompanyId(companyId);
        for (Executor exec : execs) {
            Pair<Executor, Integer> pair = new Pair<>(exec, executorToServiceRepo.findByExecutorId(exec.getId()).size());
            executorsWithServiceCount.add(pair);
        }
        return executorsWithServiceCount;
    }

    public List<ProvidedService> findExecutorsProvidedService(Integer executorId) {
        List<ExecutorToService> execWithServ = executorToServiceRepo.findByExecutorId(executorId);
        List<ProvidedService> providedServices = new ArrayList<>();
        for (ExecutorToService exec : execWithServ) {
            providedServices.add(exec.getProvidedService());
        }
        return providedServices;
    }


    public Optional<Executor> findById(Integer id) {
        return executorRepository.findById(id);
    }

    public void deleteExecutor(Integer id) {
        executorRepository.deleteById(id);
    }

    public List<Executor> findBySubstring(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            return List.of();
        }
        return executorRepository.findBySubstringInAttributes(substring);
    }

    public void assignServiceById(Executor executor, Integer serviceId) {
        ExecutorToService executorToService = new ExecutorToService();
        ProvidedService providedService = providedServiceRepo.findById(serviceId).get();
        if(executorToServiceRepo.existsByExecutorIdAndProvidedServiceId(executor.getId(), providedService.getId())) {
            return;
        }
        executorToService.setExecutor(executor);
        executorToService.setProvidedService(providedService);
        executorToServiceRepo.save(executorToService);
    }

    public List<Executor> findByService(Integer serviceId) {
        List<ExecutorToService> executorToServ = executorToServiceRepo.findByProvidedServiceId(serviceId);
        List<Executor> executors = new ArrayList<>();
        for (ExecutorToService exec : executorToServ) {
            executors.add(exec.getExecutor());
        }
        return executors;
    }

    public void assignServiceToExecutor(Integer serviceId, Executor executor) {
        Optional<ProvidedService> providedService = providedServiceRepo.findById(serviceId);
        if (providedService.isEmpty() || executor == null) {
            return;
        }
        ExecutorToService executorToService = new ExecutorToService();
        executorToService.setExecutor(executor);
        executorToService.setProvidedService(providedService.get());
        executorToServiceRepo.save(executorToService);
    }

    public List<Executor> findAll() {
        return executorRepository.findAll();
    }

    @Transactional
    public void removeServiceFromExecutor(Integer serviceId, Executor executor) {
        executorToServiceRepo.deleteByExecutorIdAndProvidedServiceId(executor.getId(), serviceId);
    }

    public List<Executor> findExecutorsByCompany(Integer id) {
        return executorRepository.findByCompanyId(id);

    }

    public void setSchedule(ExecutorSchedule executorSchedule) {
        executorScheduleRepo.save(executorSchedule);
    }

    public void saveAllSchedules(List<ExecutorSchedule> executorSchedule) {
        executorScheduleRepo.saveAll(executorSchedule);

    }

    public List<ExecutorSchedule> getSchedule(Integer id) {
        return executorScheduleRepo.findByExecutorId(id);
    }

    @Transactional
    public void deleteScheduleByIdAndWeekday(Integer id, Weekday weekday) {
        executorScheduleRepo.deleteByExecutorIdAndWeekday(id, weekday);
    }

    public Optional<ExecutorSchedule> findScheduleByIdAndWeekday(Integer id, Weekday weekday) {
        return executorScheduleRepo.findByExecutorIdAndWeekday(id, weekday);
    }

    public Optional<ExecutorToService> findExecutorAndServiceById(Integer serviceId, Integer executor_id) {
        return executorToServiceRepo.findByExecutorIdAndProvidedServiceId(executor_id, serviceId);

    }

    public List<Executor> findExecutorByCompanyAndServiceId(Integer companyId, Integer serviceId) {
        List<ExecutorToService> execToServ = executorToServiceRepo.findByProvidedServiceIdAndProvidedServiceCompanyId(serviceId, companyId);
        return execToServ.stream().map(ExecutorToService::getExecutor).toList();
    }

    public Set<Executor> findAvailableExecutors(LocalDate selectedDate, int serviceId, int company_id) {
        Set<Executor> availableExecutors = new HashSet<>();
        List<Executor> allExecutors = findExecutorByCompanyAndServiceId(company_id, serviceId);
        Weekday weekday = getWeekday(selectedDate.getDayOfWeek().toString());
        Optional<ProvidedService> providedService_opt = providedServiceRepo.findById(serviceId);
        if (providedService_opt.isEmpty()) {
            return Set.of();
        }
        ProvidedService providedService = providedService_opt.get();
        int serviceDuration = providedService.getDuration();
        for (Executor executor : allExecutors) {
            Optional<ExecutorSchedule> schedule = executorScheduleRepo.findByExecutorIdAndWeekday(executor.getId(), weekday);
            List<BookedTimeslot> bookedSlots = bookedTimeslotRepo.findBookedTimeslotsByDateAndExecutorId(selectedDate, executor.getId());
            bookedSlots.sort(Comparator.comparing(BookedTimeslot::getTimeBegin));
            if (schedule.isEmpty())
                continue;
            if (bookedSlots.isEmpty()) {
                availableExecutors.add(executor);
                continue;
            }
            LocalTime currentTime = schedule.get().getTimeBegin();
            for (BookedTimeslot bookedTimeslot : bookedSlots) {
                TimeInterval booking = new TimeInterval(bookedTimeslot.getTimeBegin().toLocalTime(), bookedTimeslot.getTimeEnd().toLocalTime());
                if (Duration.between(currentTime, booking.getStart()).toMinutes() >= serviceDuration) {
                    availableExecutors.add(executor);
                    break;
                }
                currentTime = booking.getEnd();
            }
            if (Duration.between(currentTime, schedule.get().getTimeEnd()).toMinutes() >= serviceDuration){
                availableExecutors.add(executor);
            }

        }
        return availableExecutors;
    }
    public Optional<ExecutorToService> findByExecutorAndServiceId(Integer executorId, Integer serviceId) {
        return executorToServiceRepo.findByExecutorIdAndProvidedServiceId(executorId, serviceId);
    }


}
