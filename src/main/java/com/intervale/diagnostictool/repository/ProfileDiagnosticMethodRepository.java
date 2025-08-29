package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.Profile;
import com.intervale.diagnostictool.model.ProfileDiagnosticMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileDiagnosticMethodRepository extends JpaRepository<ProfileDiagnosticMethod, Long> {
    
    @Query("SELECT pdm FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND pdm.device.id = :deviceId")
    List<ProfileDiagnosticMethod> findByProfileAndDevice(
        @Param("profileId") Long profileId, 
        @Param("deviceId") Long deviceId
    );
    
    @Query("SELECT pdm FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND " +
           "pdm.device.id = :deviceId AND " +
           "pdm.diagnosticMethod.id = :methodId")
    Optional<ProfileDiagnosticMethod> findByProfileDeviceAndMethod(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId,
        @Param("methodId") Long methodId
    );
    
    @Query("SELECT pdm FROM ProfileDiagnosticMethod pdm " +
           "JOIN FETCH pdm.diagnosticMethod dm " +
           "WHERE pdm.profile.id = :profileId")
    List<ProfileDiagnosticMethod> findByProfileWithDetails(@Param("profileId") Long profileId);
    
    @Query("SELECT pdm FROM ProfileDiagnosticMethod pdm " +
           "JOIN FETCH pdm.diagnosticMethod " +
           "WHERE pdm.profile.id = :profileId AND pdm.device.id = :deviceId")
    List<ProfileDiagnosticMethod> findByProfileAndDeviceWithDetails(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId
    );
    
    @Modifying
    @Query("DELETE FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND pdm.device.id = :deviceId")
    void deleteByProfileAndDevice(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId
    );
    
    @Modifying
    @Query("DELETE FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND " +
           "pdm.device.id = :deviceId AND " +
           "pdm.diagnosticMethod.id IN :methodIds")
    void deleteByProfileDeviceAndMethods(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId,
        @Param("methodIds") List<Long> methodIds
    );
    
    @Query("SELECT COUNT(pdm) > 0 FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND " +
           "pdm.device.id = :deviceId AND " +
           "pdm.diagnosticMethod.id = :methodId")
    boolean existsByProfileDeviceAndMethod(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId,
        @Param("methodId") Long methodId
    );
    
    @Query("SELECT DISTINCT pdm.device FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId")
    List<Device> findDevicesByProfileId(@Param("profileId") Long profileId);
    
    @Query("SELECT pdm.diagnosticMethod FROM ProfileDiagnosticMethod pdm " +
           "WHERE pdm.profile.id = :profileId AND pdm.device.id = :deviceId")
    List<com.intervale.diagnostictool.model.DiagnosticMethod> findDiagnosticMethodsByProfileAndDevice(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId
    );
}
