package com.example.auth_spring.controller;

import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.*;
import com.example.auth_spring.service.*;
import com.example.auth_spring.utils.Pair;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private CompanyService companyService;
    private ServiceManagementService serviceManagementService;
    private ExecutorService executorService;
    private UserService userService;
    private ManagerToCompanyService managerToCompanyService;
    private TextGenerationService textGenerationService;

    @GetMapping("/{id}")
    public String showCompany(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.getUserByLogin(username);
        Optional<Company> company = companyService.findById(id);
        if (company.isEmpty()) {
            return "redirect:/";
        }
            model.addAttribute("role", user.getRole());
            model.addAttribute("user", user);
            model.addAttribute("can_edit", managerToCompanyService.isManagerInCompany(user, company.get()));
            List<ProvidedService> services = serviceManagementService.findServicesByCompany(id);
            List<Pair<Executor, Integer>> executors = executorService.findExecutorsByCompanyWithCount(id);
            model.addAttribute("company", company.get());
            model.addAttribute("services", services);
            model.addAttribute("executors", executors);
        return "company";
    }

    @PostMapping("/{id}/edit")
    public String editCompany(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String home_url,
                              @RequestParam String phone,
                              @RequestParam String email)

    {
        User user = userService.getUserByLogin(userDetails.getUsername());

        if (Objects.equals(user.getRole(), Role.MANAGER.toString())& companyService.findById(id).isPresent()) {
            Company company = companyService.findById(id).get();

            if(!managerToCompanyService.isManagerInCompany(user, company)){
                return "redirect:/company/"+id;
            }
            company.setName(name);
            company.setDescription(description);
            company.setHome_url(home_url);
            company.setPhone(phone);
            company.setEmail(email);
            companyService.updateCompany(company);
        }

        return "redirect:/company/" + id;
    }

    @PostMapping("{company_id}/set_manager")
    public String setManager(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer company_id, @RequestParam String login){
        User user = userService.getUserByLogin(userDetails.getUsername());
        User userToAdd = userService.getUserByLogin(login);
        Optional<Company> company_opt = companyService.findById(company_id);
        if (company_opt.isEmpty()) {
            return "redirect:/";
        }
        Company company = company_opt.get();

        if(!user.getRole().equals(Role.MANAGER.toString()) && company.getCreatedBy()!=user){
            return "redirect:/";
        }
        try {
            companyService.addManager(userToAdd, company);
        }catch (Exception e){
            return "redirect:/company/"+ company_id;
        }

        return "redirect:/company/" + company_id;
    }

    @GetMapping("create")
    public String createCompany(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if(!user.getRole().equals(Role.MANAGER.toString())){
            return "redirect:/";
        }
        Company company = new Company();
        company.setCreatedBy(user);
        company.setName("Компания " + user.getName());
        companyService.saveCompany(company);
        managerToCompanyService.addManagerToCompany(user, company);
        model.addAttribute("company", company);
        return "redirect:/company/"+  company.getId();

    }
    @PostMapping("ask")
    public String askCompany(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam String quest) {
        String response  = textGenerationService.askQuestion("llama2:latest", quest);
        model.addAttribute("response", response);
        System.out.println(response);
        return "test_form";
    }
    @GetMapping("ask")
    public String askCompany(Model model) {
        return "test_form";
    }

}
