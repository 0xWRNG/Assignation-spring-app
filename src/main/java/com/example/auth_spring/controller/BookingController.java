package com.example.auth_spring.controller;

import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.Role;
import com.example.auth_spring.model.enums.Status;
import com.example.auth_spring.repository.ExecutorToServiceRepo;
import com.example.auth_spring.service.BookedTimeslotsService;
import com.example.auth_spring.service.ExecutorService;
import com.example.auth_spring.service.ServiceManagementService;
import com.example.auth_spring.service.UserService;
import com.example.auth_spring.utils.Pair;
import com.example.auth_spring.utils.TimeInterval;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Controller
@AllArgsConstructor
@RequestMapping("/book")
@SessionAttributes({"selectedDate", "selectedExecutor", "availableExecutors", "service", "timeslots", "user", "timeslotsWithInd"})
public class BookingController {

    private final UserService userService;
    private final ServiceManagementService serviceManagementService;
    private final ExecutorService executorService;
    private final BookedTimeslotsService bookedTimeslotsService;
    private final ExecutorToServiceRepo executorToServiceRepo;




    //region Stage1:datePick
    @GetMapping("/{company_id}/{service_id}")
    public String book(SessionStatus sessionStatus, @AuthenticationPrincipal UserDetails userDetails, @PathVariable("company_id") Integer company_id, @PathVariable("service_id") Integer service_id, Model model) {

        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() == Role.MANAGER.toString()) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        Optional<ProvidedService> providedService_opt = serviceManagementService.findById(service_id);
        if (providedService_opt.isEmpty()) {
            return "redirect:/";
        }
        ProvidedService providedService = providedService_opt.get();
        model.addAttribute("service", providedService);
        model.addAttribute("company_id", company_id);
        model.addAttribute("stage", "date_pick");

        return "booking";
    }

    @PostMapping("/{company_id}/{service_id}/date")
    public String bookDate(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable("company_id") Integer company_id,
                           @PathVariable("service_id") Integer service_id, Model model,
                           @ModelAttribute("date") String selected_date
    ) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() == Role.MANAGER.toString()) {
            return "redirect:/";
        }
        Optional<ProvidedService> providedService_opt = serviceManagementService.findById(service_id);
        if (providedService_opt.isEmpty()) {
            return "redirect:/";
        }
        if(Objects.equals(selected_date, "") || selected_date==null)
            return "redirect:/book/"+company_id+'/'+service_id;
        LocalDate date = LocalDate.parse(selected_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        Set<Executor> availableExecutors = executorService.findAvailableExecutors(date, service_id, company_id);
        model.addAttribute("selectedDate", date);
        model.addAttribute("availableExecutors", availableExecutors);
        model.addAttribute("stage", "executor_pick");

        return "booking";
    }
    //endregion Stage1:datePicked

    //region Stage2:executorPick
    @PostMapping("{company_id}/{service_id}/executor")
    public String bookExecutor(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable("company_id") Integer company_id,
                               @PathVariable("service_id") Integer service_id,
                               @ModelAttribute("executors") Integer selected_exec,
                               Model model
    ) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() == Role.MANAGER.toString()) {
            return "redirect:/";
        }
        Optional<ProvidedService> providedService_opt = serviceManagementService.findById(service_id);
        if (providedService_opt.isEmpty()) {
            return "redirect:/";
        }
        LocalDate date = (LocalDate) model.getAttribute("selectedDate");
        if (date == null) {
            return "redirect:/" + company_id + "/" + service_id;
        }

        Optional<Executor> selected_executor_opt = executorService.findById(selected_exec);
        if (selected_executor_opt.isEmpty()) {
            return "redirect:/";
        }
        List<TimeInterval> freeTimeslots = bookedTimeslotsService.getFreeTimeslots(date, selected_executor_opt.get().getId(), service_id, company_id);
        List<Pair<Integer, TimeInterval>> freeTimeslotsWithInd = IntStream.range(0, freeTimeslots.size())
                .mapToObj(i -> new Pair<>(i, freeTimeslots.get(i)))
                .toList();
        int groupSize = 3;
        List<List<Pair<Integer, TimeInterval>>> freeTimeslotsWithIndGrouped = new ArrayList<>();
        for (int i = 0; i < freeTimeslotsWithInd.size(); i += groupSize) {
            int end = Math.min(i + groupSize, freeTimeslotsWithInd.size());
            freeTimeslotsWithIndGrouped.add(new ArrayList<>(freeTimeslotsWithInd.subList(i, end)));
        }

        model.addAttribute("selectedExecutor", selected_executor_opt.get().getId());
        model.addAttribute("timeslots", freeTimeslotsWithIndGrouped);
        model.addAttribute("timeslotsWithInd", freeTimeslotsWithInd);
        model.addAttribute("stage", "timeslot_pick");

        return "booking";
    }
    //endregion Stage2:executorPick

    //region Stage3:timePick
    @PostMapping("{company_id}/{service_id}/save")
    public String bookSave(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable("company_id") Integer company_id,
                           @PathVariable("service_id") Integer service_id,
                           @ModelAttribute("timeslots_radio") Integer selected_timeslot,
                           Model model,
                           SessionStatus sessionStatus) {
        List<Pair<Integer, TimeInterval>> timeIntervals = (List<Pair<Integer, TimeInterval>>) model.getAttribute("timeslotsWithInd");
        LocalDate date = (LocalDate) model.getAttribute("selectedDate");
        assert timeIntervals != null;
        Integer executorId = (Integer) model.getAttribute("selectedExecutor");

        Optional<Executor>exec_opt = executorService.findById(executorId);
        assert executorId != null;
        if(exec_opt.isEmpty()) {
            return "redirect:/";
        }
        Executor executor = exec_opt.get();
        Optional<ExecutorToService> execToServ = executorService.findByExecutorAndServiceId(executor.getId(), service_id);
        if (timeIntervals.isEmpty() || execToServ.isEmpty() || selected_timeslot == null || selected_timeslot > timeIntervals.size()) {
            return "redirect:/" + company_id + "/" + service_id;

        }
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() == Role.MANAGER.toString()) {
            return "redirect:/";
        }
        BookedTimeslot bookedTimeslot = new BookedTimeslot();
        bookedTimeslot.setUser(user);
        bookedTimeslot.setExecutorToService(execToServ.get());
        bookedTimeslot.setTimeBegin(timeIntervals.get(selected_timeslot).second.getStart().atDate(date));
        bookedTimeslot.setTimeEnd(timeIntervals.get(selected_timeslot).second.getEnd().atDate(date));
        bookedTimeslot.setStatus(Status.not_approved);
        bookedTimeslotsService.bookTimeslot(bookedTimeslot);
        sessionStatus.setComplete();
        return "redirect:/profile";
    }
    //endregion Stage3:timePick

    @PostMapping("{company_id}/{service_id}/clr")
    public String clearForm(SessionStatus sessionStatus, @PathVariable("company_id") Integer company_id, @PathVariable("service_id") Integer service_id) {
        sessionStatus.setComplete();
        return "redirect:/book/"+company_id+'/'+service_id;
    }

    @PostMapping("delete/{booking_id}")
    public String deleteBooking(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("booking_id") Integer booking_id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if(!bookedTimeslotsService.is_belongs(user, booking_id)) {
            return "redirect:/";
        }
        bookedTimeslotsService.deleteTimeslot(booking_id);
        return "redirect:/profile";
    }

}
