package com.example.auth_spring.service;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@AllArgsConstructor
@Service
public class UserService {

    private UserRepo userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private CompanyService companyService;
    private ManagerToCompanyService managerToCompanyService;
    
    public void register(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new RuntimeException("Login already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Login not found"));
    }

    public User getUserById(int id) {
        return userRepository.getReferenceById(id);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(int id) {
        Optional<User> user_opt  = userRepository.findById(id);
        if(user_opt.isEmpty()) {
            return;
        }
        User user = user_opt.get();

        if(Objects.equals(user.getRole(), "MANAGER")){
            List<Company> managed_companies = companyService.findManagedCompanyByUserId(user);
            for(Company company : managed_companies){
                if (company.getCreatedBy()==user){
                    managerToCompanyService.removeManagerFromCompany(user, company);
                }
            }
        }
        userRepository.deleteById(id);
    }
}
