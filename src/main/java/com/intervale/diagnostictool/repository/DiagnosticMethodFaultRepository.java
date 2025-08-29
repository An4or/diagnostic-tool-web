package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.model.DiagnosticMethodFault;
import com.intervale.diagnostictool.model.FaultType;
import com.intervale.diagnostictool.model.enums.CoverageLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticMethodFaultRepository extends JpaRepository<DiagnosticMethodFault, Long> {
    
    List<DiagnosticMethodFault> findByDiagnosticMethod(DiagnosticMethod diagnosticMethod);
    
    List<DiagnosticMethodFault> findByFaultType(FaultType faultType);
    
    Optional<DiagnosticMethodFault> findByDiagnosticMethodIdAndFaultTypeId(Long diagnosticMethodId, Long faultTypeId);
    
    @Query("SELECT dmf FROM DiagnosticMethodFault dmf WHERE dmf.diagnosticMethod.id = :methodId AND dmf.effectiveness = :effectiveness")
    List<DiagnosticMethodFault> findByDiagnosticMethodIdAndEffectiveness(
            @Param("methodId") Long methodId, 
            @Param("effectiveness") CoverageLevel effectiveness);
    
    @Query("SELECT dmf FROM DiagnosticMethodFault dmf WHERE dmf.faultType.id = :faultTypeId AND dmf.effectiveness = :effectiveness")
    List<DiagnosticMethodFault> findByFaultTypeIdAndEffectiveness(
            @Param("faultTypeId") Long faultTypeId, 
            @Param("effectiveness") CoverageLevel effectiveness);
    
    @Query("SELECT COUNT(dmf) FROM DiagnosticMethodFault dmf WHERE dmf.diagnosticMethod.id = :methodId")
    long countByDiagnosticMethodId(@Param("methodId") Long methodId);
    
    @Query("SELECT COUNT(dmf) FROM DiagnosticMethodFault dmf WHERE dmf.faultType.id = :faultTypeId")
    long countByFaultTypeId(@Param("faultTypeId") Long faultTypeId);
    
    @Query("SELECT dmf FROM DiagnosticMethodFault dmf JOIN FETCH dmf.faultType ft WHERE dmf.diagnosticMethod.id IN :methodIds")
    List<DiagnosticMethodFault> findByDiagnosticMethodIds(@Param("methodIds") List<Long> methodIds);
    
    @Query("SELECT dmf FROM DiagnosticMethodFault dmf JOIN FETCH dmf.faultType ft WHERE dmf.diagnosticMethod.id = :methodId")
    List<DiagnosticMethodFault> findByDiagnosticMethodId(@Param("methodId") Long methodId);
    
    @Query("SELECT dmf FROM DiagnosticMethodFault dmf JOIN FETCH dmf.diagnosticMethod dm WHERE dmf.faultType.id = :faultTypeId")
    List<DiagnosticMethodFault> findByFaultTypeId(@Param("faultTypeId") Long faultTypeId);
    
    @Modifying
    @Query("DELETE FROM DiagnosticMethodFault dmf WHERE dmf.diagnosticMethod.id = :methodId AND dmf.faultType.id = :faultTypeId")
    void deleteByDiagnosticMethodIdAndFaultTypeId(
            @Param("methodId") Long methodId, 
            @Param("faultTypeId") Long faultTypeId);
}
