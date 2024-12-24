package com.example.auth_spring.controller;

import com.example.auth_spring.model.BookedTimeslot;
import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.User;
import com.example.auth_spring.service.*;
import com.example.auth_spring.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    private final BookedTimeslotsService bookedTimeslotsService;
    private final ManagerToCompanyService managerToCompanyService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public ProfileController(BookedTimeslotsService bookedTimeslotsService, ManagerToCompanyService managerToCompanyService) {
        this.bookedTimeslotsService = bookedTimeslotsService;
        this.managerToCompanyService = managerToCompanyService;
    }


    @GetMapping("")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user;
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        user = userService.getUserByLogin(username);
        model.addAttribute("user", user);
        switch (user.getRole()) {
            case "USER":
                List<BookedTimeslot> userTimeslots = bookedTimeslotsService.findByUser(user.getId());
                model.addAttribute("services", userTimeslots);
            case "MANAGER":
                List<Pair<Company, List<User>>> managedCompanies = managerToCompanyService.getCompanyWithRelatedManagers(user);

                model.addAttribute("companies", managedCompanies);
        }


        Class<?> clazz = user.getClass();
        Field[] fields = clazz.getDeclaredFields();


        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(user);
                model.addAttribute(field.getName(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("user", user);
        return "profile"; // Страница профиля
    }

    @GetMapping("edit")
    public String showEditProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user;
        if (userDetails == null) {
            return "redirect:/login";
        }
        String username = userDetails.getUsername();
        user = userService.getUserByLogin(username);

        model.addAttribute("edit", true);
        model.addAttribute("user", user);
        switch (user.getRole()) {
            case "USER":
                List<BookedTimeslot> userTimeslots = bookedTimeslotsService.findByUser(user.getId());
                model.addAttribute("services", userTimeslots);
            case "MANAGER":
                List<Pair<Company, List<User>>> managedCompanies = managerToCompanyService.getCompanyWithRelatedManagers(user);

                model.addAttribute("companies", managedCompanies);
        }


        Class<?> clazz = user.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(user);
                model.addAttribute(field.getName(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "profile"; // Страница профиля
    }

    @PostMapping("edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model,
                              @RequestParam String surname,
                              @RequestParam String name,
                              @RequestParam String patronymic,
                              @RequestParam String email,
                              @RequestParam String phone
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String username = userDetails.getUsername();
        User user = userService.getUserByLogin(username);

        user.setSurname(surname);
        user.setName(name);
        user.setPatronymic(patronymic);
        user.setEmail(email);
        user.setPhone(phone);

        userService.saveUser(user);
        return "redirect:/profile";

    }

    @PostMapping("leave_company/{company_id}")
    public String leaveCompany(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer company_id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        Optional<Company> managed_company_opt = companyService.findById(company_id);
        if (user.getRole() != "MANAGER" || managed_company_opt.isEmpty()) {
            return "redirect:/profile";
        }
        Company managed_company = managed_company_opt.get();
        if (!managerToCompanyService.isManagerInCompany(user, managed_company)) {
            return "redirect:/profile";
        }

        managerToCompanyService.removeManagerFromCompany(user, managed_company);

        return "redirect:/profile";


    }

}
/*TODO
 *Validation while editing
 */