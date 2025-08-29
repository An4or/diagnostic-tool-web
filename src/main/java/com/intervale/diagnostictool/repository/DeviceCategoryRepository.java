package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.DeviceCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {
    
    @Query("SELECT DISTINCT c FROM DeviceCategory c LEFT JOIN FETCH c.devices")
    List<DeviceCategory> findAllWithDevices();
    
    @EntityGraph(attributePaths = {"devices", "diagnosticMethods"})
    @Query("SELECT c FROM DeviceCategory c WHERE c.id = :id")
    Optional<DeviceCategory> findByIdWithDevicesAndMethods(@Param("id") Long id);
    
    @Query("SELECT DISTINCT c FROM DeviceCategory c LEFT JOIN FETCH c.diagnosticMethods")
    List<DeviceCategory> findAllWithDiagnosticMethods();
    
    @Query("SELECT c FROM DeviceCategory c LEFT JOIN FETCH c.devices WHERE c.id = :id")
    Optional<DeviceCategory> findByIdWithDevices(@Param("id") Long id);
    
    @Query("SELECT c FROM DeviceCategory c LEFT JOIN FETCH c.diagnosticMethods WHERE c.id = :id")
    Optional<DeviceCategory> findByIdWithDiagnosticMethods(@Param("id") Long id);
    
    Optional<DeviceCategory> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.category = :category")
    boolean hasDevices(@Param("category") DeviceCategory category);
    
    @Query("SELECT COUNT(dm) > 0 FROM DiagnosticMethod dm WHERE dm.deviceCategory = :category")
    boolean hasDiagnosticMethods(@Param("category") DeviceCategory category);
}
