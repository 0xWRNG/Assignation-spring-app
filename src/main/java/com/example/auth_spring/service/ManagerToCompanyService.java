package com.example.auth_spring.service;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.ManagerToCompany;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.CompanyRepo;
import com.example.auth_spring.repository.ManagerToCompanyRepo;
import com.example.auth_spring.repository.UserRepo;
import com.example.auth_spring.utils.Pair;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerToCompanyService {
    private static final Logger logger = LoggerFactory.getLogger(ManagerToCompanyService.class);
    private final ManagerToCompanyRepo managerToCompanyRepo;
    private final CompanyRepo companyRepo;
    private final UserRepo userRepo;

    @Autowired
    public ManagerToCompanyService(ManagerToCompanyRepo managerToCompanyRepo, CompanyRepo companyRepo, UserRepo userRepo) {
        this.managerToCompanyRepo = managerToCompanyRepo;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
    }

    public void addManagerToCompany(User manager, Company company) {
        ManagerToCompany association = new ManagerToCompany(manager, company);
        managerToCompanyRepo.save(association);
    }





//    @Transactional
//    public void removeManagerFromCompany(User user, Company company_param) {
//        Optional<Company> company_opt =  companyRepo.findById(company_param.getId());
//        if(company_opt.isEmpty()) {
//            return;
//        }
//        Company company =   company_opt.get();
//
//        logger.info("Removing manager {} from company {}", user, company);
//
//        if (user.equals(company.getCreatedBy())) {
//            User newCreator = getNewCreator(company);
//
//            if (newCreator != null&& newCreator!=user) {
//                company.setCreatedBy(newCreator);
//                companyRepo.save(company);
//
//                managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
//                logger.info("Successfully removed manager {} from company {}. New creator is {}", user, company, newCreator);
//                return;
//            } else {
//                company.setCreatedBy(null);
//                companyRepo.save(company);
//                managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
//                companyRepo.delete(company);
//                logger.info("No managers left in company {}. Deleted company.", company);
//                return;
//            }
//        }
//        managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
//    }

public void removeManagerFromCompany(User user, Company company_param) {
    Optional<Company> companyOpt = companyRepo.findById(company_param.getId());
    if (companyOpt.isEmpty()) {
        logger.warn("Company with id {} not found. Exiting.", company_param.getId());
        return;
    }

    Company company = companyOpt.get();
    logger.info("Removing manager {} from company {}", user, company);

    if (user.equals(company.getCreatedBy())) {
        User newCreator = getNewCreator(company);

        if (newCreator != null && !newCreator.equals(user)) {
            // Назначаем нового создателя компании
            company.setCreatedBy(newCreator);
            companyRepo.save(company);

            // Удаляем связь менеджера с компанией
            managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
            logger.info("Successfully removed manager {} from company {}. New creator is {}", user, company, newCreator);
            return;
        } else {
            // Если нового создателя нет, удаляем компанию
            company.setCreatedBy(null);
            companyRepo.save(company);

            managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
            companyRepo.deleteById(company.getId());
            logger.info("No managers left in company {}. Deleted company.", company);
            return;
        }
    }

    // Удаляем связь менеджера с компанией
    managerToCompanyRepo.deleteAssociation(user.getId(), company.getId());
}

    private User getNewCreator(Company company) {
        List<ManagerToCompany> managers = getManagersByCompany(company);
        if (managers.isEmpty()) {
            return null;
        }

        for (ManagerToCompany manager : managers) {
            if (!manager.getManager().getId().equals(company.getCreatedBy().getId())) {
                return manager.getManager();
            }
        }
        return managers.get(0).getManager();
    }


    public List<ManagerToCompany> getManagersByCompany(Company company) {
        return managerToCompanyRepo.findByCompany(company);
    }

    public List<Pair<Company, List<User>>> getCompanyWithRelatedManagers(User manager) {
        List<Pair<Company, List<User>>> companyWithRelatedManagers = new ArrayList<>();

        List<ManagerToCompany> managerToCompanies = managerToCompanyRepo.findByManager(manager);

        for (ManagerToCompany managerToCompany : managerToCompanies) {
            Company company = managerToCompany.getCompany();

            // Находим всех менеджеров, связанных с этой компанией
            List<User> managers = new ArrayList<>();
            for (ManagerToCompany companyToManager : managerToCompanyRepo.findByCompany(company)) {
                managers.add(companyToManager.getManager());
            }

            // Создаем пару и добавляем в список
            Pair<Company, List<User>> pair = new Pair<>(company, managers);
            companyWithRelatedManagers.add(pair);
        }

        return companyWithRelatedManagers;
    }

    public boolean isManagerInCompany(User manager, Company company) {
        List<ManagerToCompany> managerToCompanies = managerToCompanyRepo.findByManager(manager);
        for (ManagerToCompany managerToCompany : managerToCompanies) {
            if (managerToCompany.getCompany().equals(company)) {
                return true;
            }
        }
        return false;
    }
}