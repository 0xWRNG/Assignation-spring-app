package com.example.auth_spring.controller;

import com.example.auth_spring.model.*;
import com.example.auth_spring.model.enums.Role;
import com.example.auth_spring.model.enums.Weekday;
import com.example.auth_spring.repository.ExecutorScheduleRepo;
import com.example.auth_spring.service.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/executor")
@SessionAttributes({"weekdays", "company_id"})
@AllArgsConstructor
public class ExecutorController {
    private ExecutorService executorService;
    private ManagerToCompanyService managerToCompanyService;
    private UserService userService;
    private ServiceManagementService serviceManagementService;
    private CompanyService companyService;
    private ExecutorScheduleRepo executorScheduleRepo;


    public String getWeekday(Weekday weekday) {
        return switch (weekday) {
            case mon -> "Понедельник";
            case tue -> "Вторник";
            case wed -> "Среда";
            case thu -> "Четверг";
            case fri -> "Пятница";
            case sat -> "Суббота";
            case sun -> "Воскресенье";
        };
    }

    @GetMapping("/{id}")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, Model model) {
        Optional<Executor> exec = executorService.findById(id);
        if (exec.isEmpty()) {
            return "redirect:/";
        }
        List<ExecutorSchedule> schedules = executorService.getSchedule(id);

        Map<String, ExecutorSchedule> scheduleMap = new HashMap<>();
        for (Weekday weekday : Weekday.values()) {
            scheduleMap.put(getWeekday(weekday), null);
        }

        for (ExecutorSchedule schedule : schedules) {
            scheduleMap.put(schedule.getWeekday().toString(), schedule);
        }
        Map<String, String> weekdayMap = new HashMap<>();
        for (Weekday weekday : Weekday.values()) {
            weekdayMap.put(weekday.toString(), getWeekday(weekday));
        }
        model.addAttribute("weekdays", weekdayMap);


        List<ProvidedService> services = executorService.findExecutorsProvidedService(id);
        model.addAttribute("executor", exec.get());
        model.addAttribute("services", services);
        model.addAttribute("schedule", scheduleMap);

        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);

        return "executor_profile";
    }

    @GetMapping("/{id}/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, Model model) {
        Optional<Executor> exec = executorService.findById(id);
        User user = userService.getUserByLogin(userDetails.getUsername());
        model.addAttribute("user", user);
        if (exec.isEmpty() || !Objects.equals(user.getRole(), Role.MANAGER.toString())) {
            return "redirect:/";
        }
        if (!managerToCompanyService.isManagerInCompany(user, exec.get().getCompany())) {
            return "redirect:/";
        }
        List<ProvidedService> services = executorService.findExecutorsProvidedService(id);
        List<ProvidedService> services_of_company = serviceManagementService.findServicesByCompany(exec.get().getCompany().getId());
        model.addAttribute("company_id", exec.get().getCompany().getId());
        Map<Integer, Boolean> checked_services = new HashMap<>();
        for (ProvidedService service : services_of_company) {
            if (services.contains(service)) {
                checked_services.put(service.getId(), true);
            } else {
                checked_services.put(service.getId(), false);
            }

        }
        List<ExecutorSchedule> schedules = executorService.getSchedule(id);

        Map<String, ExecutorSchedule> scheduleMap = new HashMap<>();
        for (Weekday weekday : Weekday.values()) {
            scheduleMap.put(weekday.toString(), null);
        }

        for (ExecutorSchedule schedule : schedules) {
            scheduleMap.put(schedule.getWeekday().toString(), schedule);
        }
        model.addAttribute("executor", exec.get());
        model.addAttribute("schedule", scheduleMap);
        model.addAttribute("services", services_of_company);
        model.addAttribute("checked_services", checked_services);
        model.addAttribute("user", user);
        model.addAttribute("edit", true);

        return "executor_profile";
    }

    @GetMapping("/{id}/add")
    public String addExecutor(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, Model model) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (!user.getRole().equals(Role.MANAGER.toString())) {
            return "redirect:/";
        }
        List<ProvidedService> services = serviceManagementService.findServicesByCompany(id);
        model.addAttribute("company_id", id);
        model.addAttribute("edit", true);
        model.addAttribute("services", services);
        model.addAttribute("user", user);
        return "executor_profile";
    }

    @PostMapping("/{company_id}/add")
    public String add(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer company_id, @ModelAttribute Executor executor, @RequestParam(value = "services", required = false) List<String> services) {
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (!user.getRole().equals(Role.MANAGER.toString())) {
            return "redirect:/";
        }
        Executor executorToSave = new Executor();
        Class<?> clazz = executorToSave.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(executor);
                if (value != null) {
                    field.set(executorToSave, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Optional<Company> comp_opt = companyService.findById(company_id);
        if (!comp_opt.isPresent()) {
            return "redirect:/";
        }
        Company comp = comp_opt.get();
        executorToSave.setCompany(comp);
        executorService.saveExecutor(executorToSave);
        if (services != null) {
            for (String service_id : services) {

                executorService.assignServiceById(executorToSave, Integer.parseInt(service_id));
            }
        }
        return "redirect:/company/" + company_id;
    }

    @PostMapping("{id}/edit")
    public String edit(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, @ModelAttribute Executor executor, @RequestParam(value = "services", required = false) List<Integer> selected_services, Model model) {
        Optional<Executor> get_executor = executorService.findById(id);
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (get_executor.isEmpty() || !user.getRole().equals(Role.MANAGER.toString())) {
            return "redirect:/";
        }
        Integer company_id = (Integer) model.getAttribute("company_id");
        List<ProvidedService> services = serviceManagementService.findServicesByCompany(company_id);
        if (selected_services != null) {

            for (Integer selected_service : selected_services) {
                boolean exists = services.stream()
                        .anyMatch(service -> service.getId().equals(selected_service));

                if (!exists) {
                    return "redirect:/";
                }
            }
        }

        Executor executor_not_updated = get_executor.get();
        Class<?> clazz = executor_not_updated.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(executor);
                if (value == null) {
                    continue;
                }
                field.set(executor_not_updated, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.saveExecutor(executor_not_updated);
        if (selected_services != null) {

            for (ProvidedService service : services) {
                if (selected_services.contains(service.getId())) {
                    executorService.assignServiceById(executor_not_updated, service.getId());
                } else {
                    executorService.removeServiceFromExecutor(service.getId(), executor_not_updated);
                }
            }
        }
        return "redirect:/executor/" + id;
    }

    @PostMapping("/{id}/edit/schedule")
    public String editSchedule(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id,
                               @RequestParam Map<String, String> params, Model model) {
        Optional<Executor> executorOpt = executorService.findById(id);
        if (executorOpt.isEmpty()) {
            return "redirect:/";
        }

        Executor executor = executorOpt.get();
        List<ExecutorSchedule> schedules = new ArrayList<>();

        for (Weekday weekday : Weekday.values()) {
            String startParam = params.get(weekday.name() + "_start");
            String endParam = params.get(weekday.name() + "_end");
            String offParam = params.get(weekday.name() + "_off");

            if (offParam != null || (Objects.equals(startParam, "") && Objects.equals(endParam, ""))) { //in case weekday set to weekend
                executorService.deleteScheduleByIdAndWeekday(id, weekday);
                continue;
            }
            LocalTime startTime = LocalTime.parse(startParam);

            LocalTime endTime = LocalTime.parse(endParam);
            Optional<ExecutorSchedule> currentSchedule = executorService.findScheduleByIdAndWeekday(id, weekday);
            if (currentSchedule.isPresent()) {
                ExecutorSchedule updatedSchedule = currentSchedule.get();
                updatedSchedule.setWeekday(weekday);
                updatedSchedule.setTimeBegin(startTime);
                updatedSchedule.setTimeEnd(endTime);
                schedules.add(updatedSchedule);
            } else {
                ExecutorSchedule schedule = new ExecutorSchedule();
                schedule.setExecutor(executor);
                schedule.setWeekday(weekday);
                schedule.setTimeBegin(startTime);
                schedule.setTimeEnd(endTime);
                schedules.add(schedule);
            }
        }

        executorService.saveAllSchedules(schedules);

        return "redirect:/executor/" + id;
    }


    @PostMapping("delete/{id}")
    public String deleteExecutor(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        Optional<Executor> get_executor = executorService.findById(id);
        User user = userService.getUserByLogin(userDetails.getUsername());
        Optional<Executor> executorToDelete = executorService.findById(id);
        Integer company_id;
        if (user.getRole().equals(Role.MANAGER.toString()) && executorToDelete.isPresent()) {
            Executor executor = executorToDelete.get();
            company_id = executor.getCompany().getId();
            executorService.deleteExecutor(id);
            return "redirect:/company/" + company_id;
        }
        return "redirect:/";
    }
}
/*TODO
 * Do schedule display
 * */