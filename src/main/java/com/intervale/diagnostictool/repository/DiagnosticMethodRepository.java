package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticMethodRepository extends JpaRepository<DiagnosticMethod, Long> {
    List<DiagnosticMethod> findByDeviceCategory(DeviceCategory category);
    List<DiagnosticMethod> findByDeviceCategoryId(Long categoryId);
    boolean existsByNameAndDeviceCategory(String name, DeviceCategory category);
    
    @Query("SELECT COUNT(dm) FROM DiagnosticMethod dm WHERE dm.deviceCategory = :category")
    long countByDeviceCategory(@Param("category") DeviceCategory category);
    
    @Query("SELECT DISTINCT dm FROM DiagnosticMethod dm " +
           "JOIN DiagnosticMethodFault dmf ON dmf.diagnosticMethod.id = dm.id " +
           "WHERE dmf.faultType.id = :faultTypeId")
    List<DiagnosticMethod> findByFaultTypeId(@Param("faultTypeId") Long faultTypeId);
}
