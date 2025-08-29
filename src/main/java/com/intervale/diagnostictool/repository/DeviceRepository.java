package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    List<Device> findByCategoryId(Long categoryId);
    
    List<Device> findByCategory(DeviceCategory category);
    
    List<Device> findByCategoryIsNull();
    
    List<Device> findAllByIdIn(Set<Long> ids);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndCategory(String name, DeviceCategory category);
    
    @Query("SELECT d FROM Device d JOIN FETCH d.category WHERE d.id = :id")
    Optional<Device> findByIdWithCategory(@Param("id") Long id);
    
    @Query("SELECT DISTINCT d FROM Device d " +
           "LEFT JOIN FETCH d.diagnosticMethods dm " +
           "LEFT JOIN FETCH d.category " +
           "WHERE d.id IN :ids")
    List<Device> findByIdsWithDiagnosticMethods(@Param("ids") Set<Long> ids);
    
    @Query("SELECT d FROM Device d " +
           "LEFT JOIN FETCH d.diagnosticMethods dm " +
           "LEFT JOIN FETCH d.category c " +
           "LEFT JOIN FETCH c.diagnosticMethods " +
           "WHERE d.id = :id")
    Optional<Device> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.category = :category")
    long countByCategory(@Param("category") DeviceCategory category);
    
    @Query("SELECT DISTINCT d FROM Device d JOIN d.diagnosticMethods dm WHERE dm.id = :methodId")
    List<Device> findByDiagnosticMethodsId(@Param("methodId") Long methodId);
}
