package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.model.FaultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultTypeRepository extends JpaRepository<FaultType, Long> {
    
    List<FaultType> findByDeviceCategory(DeviceCategory deviceCategory);
    
    List<FaultType> findByDeviceCategoryId(Long deviceCategoryId);
    
    @Query("SELECT ft FROM FaultType ft WHERE ft.deviceCategory.id IN :categoryIds")
    List<FaultType> findByDeviceCategoryIds(@Param("categoryIds") List<Long> categoryIds);
    
    @Query("SELECT ft FROM FaultType ft JOIN FETCH ft.diagnosticMethods dm WHERE dm.diagnosticMethod.id = :methodId")
    List<FaultType> findByDiagnosticMethodId(@Param("methodId") Long methodId);
    
    @Query("SELECT ft FROM FaultType ft WHERE LOWER(ft.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(ft.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FaultType> search(@Param("query") String query);
}
