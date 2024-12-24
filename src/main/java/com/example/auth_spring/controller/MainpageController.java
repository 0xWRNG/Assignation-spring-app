package com.example.auth_spring.controller;


import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.Role;
import com.example.auth_spring.model.enums.Status;
import com.example.auth_spring.service.*;
import com.example.auth_spring.utils.Pair;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/")
@AllArgsConstructor
@SessionAttributes("keys")
public class MainpageController {

    private UserService userService;
    private ServiceManagementService serviceManagementService;
    private ExecutorService executorService;
    private CompanyService companyService;
    private BookedTimeslotsService bookedTimeslotsService;


    @GetMapping("manage-assigns")
    public String manageAssigns(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        if(!Objects.equals(user.getRole(), "MANAGER")){
            return "redirect:/";
        }
        List<Company> companies = companyService.findManagedCompanyByUserId(user);
        if (companies == null) {
            return "redirect:/profile";
        }
        ArrayList<Pair<String, String>> keys = new ArrayList<>();
        keys.add(new Pair<>("approved", "Подтвержденные"));
        keys.add(new Pair<>("not_approved", "Неподтвержденные"));
        keys.add(new Pair<>("canceled", "Отмененные"));
        model.addAttribute("keys", keys);
        Map<String, List<BookedTimeslot>> allBooks = new HashMap<>();
        for (Company company : companies) {
            for (Status status : Status.values()) {
                if (!allBooks.containsKey(status.toString())) {
                    allBooks.put(status.toString(), bookedTimeslotsService.findByStatusAndCompanyId(status, company.getId()));
                }
            }
        }
        model.addAttribute("companies", companies);
        model.addAttribute("bookings", allBooks);
        return "manage-assigns";
    }


    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        return "mainpage";
    }

    @PostMapping("")
    public String search(@AuthenticationPrincipal UserDetails userDetails, Model model, @RequestParam String request, @RequestParam String type) {
        User user = userService.getUserByLogin(userDetails.getUsername());

            int result_size = 0;
            model.addAttribute("request", request);
            model.addAttribute("type", type);
            switch (type) {
                case "serviceName":
                    List<ProvidedService> serviceList = serviceManagementService.findBySubstringInTitle(request);
                    result_size += serviceList.size();
                    model.addAttribute("services", serviceList);
                    break;
                case "serviceDescription":
                    serviceList = serviceManagementService.findBySubstringInDescription(request);
                    result_size += serviceList.size();
                    model.addAttribute("services", serviceList);

                    break;
                case "companyName":
                    List<Company> companies = companyService.findBySubstring(request);
                    result_size += companies.size();
                    model.addAttribute("companies", companies);
                    break;
                case "executor":
                    List<Executor> executors = executorService.findBySubstring(request);
                    result_size += executors.size();
                    model.addAttribute("executors", executors);
                    break;

                default:
                    break;
            }
        model.addAttribute("results", result_size);
        model.addAttribute("user", user);
        return "mainpage";
    }
    @PostMapping("/update-status/{booking_id}")
    public ResponseEntity<String> updateStatus(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer booking_id, @RequestParam String status) {

        User user = userService.getUserByLogin(userDetails.getUsername());
        if (!Objects.equals(user.getRole(), "MANAGER")) {
            return ResponseEntity.badRequest().build();
        }

        Optional<BookedTimeslot> timeslot = bookedTimeslotsService.findById(booking_id);
        if (timeslot.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        timeslot.get().setStatus(Status.valueOf(status));
        bookedTimeslotsService.bookTimeslot(timeslot.get());
        return ResponseEntity.ok().build();
    }
}
