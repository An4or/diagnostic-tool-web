package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.Profile;
import com.intervale.diagnostictool.model.ProfileFault;
import com.intervale.diagnostictool.model.ProfileFaultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileFaultRepository extends JpaRepository<ProfileFault, ProfileFaultId> {
    
    List<ProfileFault> findByProfile(Profile profile);
    
    List<ProfileFault> findByProfileId(Long profileId);
    
    Optional<ProfileFault> findByProfileIdAndFaultTypeId(Long profileId, Long faultTypeId);
    
    @Query("SELECT pf FROM ProfileFault pf WHERE pf.profile.id = :profileId AND pf.faultType.deviceCategory.id = :categoryId")
    List<ProfileFault> findByProfileIdAndDeviceCategoryId(@Param("profileId") Long profileId, 
                                                         @Param("categoryId") Long categoryId);
    
    @Query("SELECT pf FROM ProfileFault pf WHERE pf.profile.id = :profileId AND pf.isCovered = :isCovered")
    List<ProfileFault> findByProfileIdAndCovered(@Param("profileId") Long profileId, 
                                               @Param("isCovered") boolean isCovered);
                                               
    @Query("SELECT pf FROM ProfileFault pf " +
           "JOIN pf.faultType ft " +
           "JOIN ft.deviceCategory dc " +
           "JOIN dc.devices d " +
           "WHERE pf.profile.id = :profileId " +
           "AND d.id = :deviceId " +
           "AND ft.id = :faultTypeId")
    Optional<ProfileFault> findByProfileIdAndDeviceIdAndFaultTypeId(
            @Param("profileId") Long profileId,
            @Param("deviceId") Long deviceId,
            @Param("faultTypeId") Long faultTypeId
    );

    
    @Modifying
    @Query("UPDATE ProfileFault pf SET pf.isCovered = :isCovered, pf.coveredMethodsIds = :coveredMethodsIds, pf.notes = :notes WHERE pf.id = :id")
    void updateCoverage(@Param("id") ProfileFaultId id, 
                       @Param("isCovered") boolean isCovered, 
                       @Param("coveredMethodsIds") String coveredMethodsIds, 
                       @Param("notes") String notes);
    
    @Query("SELECT COUNT(pf) FROM ProfileFault pf WHERE pf.profile.id = :profileId AND pf.isCovered = true")
    long countCoveredFaultsByProfileId(@Param("profileId") Long profileId);
    
    @Query("SELECT COUNT(pf) FROM ProfileFault pf WHERE pf.profile.id = :profileId")
    long countTotalFaultsByProfileId(@Param("profileId") Long profileId);
}
