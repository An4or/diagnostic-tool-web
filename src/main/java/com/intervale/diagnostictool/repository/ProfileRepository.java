package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    @EntityGraph(attributePaths = {"devices", "deviceDiagnosticMethods"})
    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.devices WHERE p.id = :id")
    Optional<Profile> findByIdWithDevices(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"devices", "deviceDiagnosticMethods"})
    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.devices")
    List<Profile> findAllWithDevices();
    
    @Query("SELECT p.devices FROM Profile p WHERE p.id = :profileId")
    Set<Device> findDevicesByProfileId(@Param("profileId") Long profileId);
    
    @Query("SELECT p FROM Profile p JOIN FETCH p.deviceDiagnosticMethods pdm " +
           "JOIN FETCH pdm.device d JOIN FETCH pdm.diagnosticMethod dm " +
           "WHERE p.id = :profileId")
    Optional<Profile> findByIdWithDiagnosticMethods(@Param("profileId") Long profileId);
    
    @Query("SELECT p FROM Profile p " +
           "LEFT JOIN FETCH p.deviceDiagnosticMethods pdm " +
           "LEFT JOIN FETCH pdm.device " +
           "LEFT JOIN FETCH pdm.diagnosticMethod " +
           "WHERE p.id = :id")
    Optional<Profile> findByIdWithAllDetails(@Param("id") Long id);
    
    @Query("SELECT COUNT(p) > 0 FROM Profile p JOIN p.devices d WHERE d.id = :deviceId")
    boolean isDeviceUsedInProfiles(@Param("deviceId") Long deviceId);
    
    @Modifying
    @Query("DELETE FROM Profile p WHERE p.id = :profileId")
    void deleteProfileAndRelations(@Param("profileId") Long profileId);
    
    Optional<Profile> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(p) > 0 FROM Profile p JOIN p.devices d WHERE d.id = :deviceId AND p.id = :profileId")
    boolean isDeviceInProfile(
        @Param("profileId") Long profileId,
        @Param("deviceId") Long deviceId
    );
}
