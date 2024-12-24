package com.example.auth_spring.controller;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.Executor;
import com.example.auth_spring.model.ProvidedService;
import com.example.auth_spring.model.User;
import com.example.auth_spring.model.enums.Role;
import com.example.auth_spring.service.*;
import com.example.auth_spring.utils.Pair;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/service")
@AllArgsConstructor
@SessionAttributes("user")
public class ServiceController {
    private final ManagerToCompanyService managerToCompanyService;
    private UserService userService;
    private ServiceManagementService serviceManagementService;
    private CompanyService companyService;
    private ExecutorService executorService;

    //region modify
    @GetMapping("/modify/{service_id}")
    public String showModify(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable Integer service_id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() != Role.MANAGER.toString()) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        Optional<ProvidedService> providedService = serviceManagementService.findById(service_id);
        if (providedService.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("service", providedService.get());
        model.addAttribute("mode", "modify");
              managerToCompanyService.isManagerInCompany(user, providedService.get().getCompany());
        List<Executor> executorsWithCurrentService = executorService.findByService(service_id);
        List<Executor> executorsAll = executorService.findExecutorsByCompany(providedService.get().getCompany().getId());
        List<Pair<Boolean, Executor>> executorsToForm = new ArrayList<>();
        for (Executor executor : executorsAll) {
            if (executorsWithCurrentService.contains(executor)) {
                executorsToForm.add(new Pair<>(true, executor));
            } else {
                executorsToForm.add(new Pair<>(false, executor));
            }
        }
        model.addAttribute("executors", executorsToForm);
        return "service";
    }

    @PostMapping("modify/{service_id}")
    public String applyModify(@AuthenticationPrincipal UserDetails userDetails, Model model,
                              @PathVariable Integer service_id,
                              @ModelAttribute ProvidedService providedService,
                              @RequestParam(value = "executors", required = false) List<String> executors
    ) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() != Role.MANAGER.toString()) {
            return "redirect:/";
        }
        Optional<ProvidedService> modifiedService_opt = serviceManagementService.findById(service_id);
        if (modifiedService_opt.isEmpty()) {
            return "redirect:/";
        }

        ProvidedService modifiedService = modifiedService_opt.get();
        Class<?> clazz = modifiedService.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName() == "id") {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(providedService);
                if (value != null) {
                    field.set(modifiedService, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        List<Executor> currentExecutors = executorService.findByService(service_id);
        for (Executor executor : currentExecutors) {
            if (executors == null || !executors.contains(executor.getId().toString())) {
                executorService.removeServiceFromExecutor(service_id, executor); // метод для удаления связи
            }
        }

        if (executors != null) {
            for (String executorId : executors) {
                boolean alreadyAssigned = currentExecutors.stream()
                        .anyMatch(e -> e.getId().toString().equals(executorId));
                if (!alreadyAssigned) {
                    Optional<Executor> executor = executorService.findById(Integer.parseInt(executorId));
                    executor.ifPresent(value -> executorService.assignServiceToExecutor(service_id, value));
                }
            }
        }
        serviceManagementService.saveService(modifiedService);
        return "redirect:/service/" + service_id;
    }

    @PostMapping("delete/{service_id}")
    public String delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer service_id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user.getRole() != Role.MANAGER.toString()) {
            return "redirect:/";
        }
        Optional<ProvidedService> serviceToDelete = serviceManagementService.findById(service_id);
        if (serviceToDelete.isEmpty()) {
            return "redirect:/";
        }
        Integer company_id = serviceToDelete.get().getCompany().getId();
        serviceManagementService.deleteService(service_id);
        return "redirect:/company/" + company_id;
    }

    //endregion modify

    //region show
    @GetMapping("{id}")
    public String showService(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable Integer id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        Optional<ProvidedService> currentServiceservice = serviceManagementService.findById(id);
        currentServiceservice.ifPresent(providedService -> model.addAttribute("service", providedService));
        List<Executor> executors = executorService.findByService(id);
        model.addAttribute("executors", executors);

        model.addAttribute("mode", "show");
        return "service";
    }
    //endregion show

    //region add
    @GetMapping("add/{id}")
    public String add_get_Service(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable Integer id) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        if(user.getRole() != Role.MANAGER.toString()) {
            return "redirect:/";
        }
//        Optional<ProvidedService> currentServiceservice = serviceManagementService.findById(id);
//        currentServiceservice.ifPresent(providedService -> model.addAttribute("service", providedService));
        List<Executor> executors = executorService.findExecutorsByCompany(id);
        List<Pair<Boolean, Executor>> executorsToForm = new ArrayList<>();
        for (Executor executor : executors) {
            executorsToForm.add(new Pair<>(false, executor));
        }
        model.addAttribute("executors", executorsToForm);
        model.addAttribute("mode", "add");
        return "service";
    }

    @PostMapping("add/{id}")
    public String add_post_Service(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable Integer id, @ModelAttribute ProvidedService providedService, @RequestParam(value = "executors", required = false) List<String> executors) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        if (user.getRole() != Role.MANAGER.toString()) {
            return "redirect:/";
        }
        if (id == null) {
            return "redirect:/";
        }
        ProvidedService providedService_toSave = new ProvidedService();
        Optional<Company> company_opt = companyService.findById(id);
        if (company_opt.isPresent()) {
            Company company = company_opt.get();
            providedService_toSave.setCompany(company);
        }
        Class<?> clazz = providedService_toSave.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName() == "id") {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(providedService);
                if (value != null) {
                    field.set(providedService_toSave, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        serviceManagementService.saveService(providedService_toSave);
        if (executors != null) {
            for (String executor_id : executors) {
                Optional<Executor> foundExecutor = executorService.findById(Integer.parseInt(executor_id));
                if (foundExecutor.isEmpty()) {
                    continue;
                }
                executorService.assignServiceToExecutor(providedService_toSave.getId(), foundExecutor.get());
            }
        }

        return "redirect:/service/" + providedService_toSave.getId();
    }
    //endregion
}
