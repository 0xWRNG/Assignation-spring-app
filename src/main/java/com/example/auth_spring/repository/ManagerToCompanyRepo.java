package com.example.auth_spring.repository;


import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.ManagerToCompany;
import com.example.auth_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface ManagerToCompanyRepo extends JpaRepository<ManagerToCompany, Integer> {
    List<ManagerToCompany> findByCompany(Company company);
    List<ManagerToCompany> findByManager(User manager);

    @Modifying
    @Transactional
    @Query("DELETE FROM ManagerToCompany mc WHERE mc.manager.id = :managerId AND mc.company.id = :companyId")
    void deleteAssociation(Integer managerId, Integer companyId);
    @Query("SELECT COUNT(m) FROM ManagerToCompany m WHERE m.company = :company")
    Integer getManagerCount(@Param("company") Company company);

}
