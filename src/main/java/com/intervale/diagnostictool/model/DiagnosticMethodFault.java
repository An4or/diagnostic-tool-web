package com.intervale.diagnostictool.model;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "diagnostic_method_faults")
public class DiagnosticMethodFault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_method_id", nullable = false)
    private DiagnosticMethod diagnosticMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fault_type_id", nullable = false)
    private FaultType faultType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoverageLevel effectiveness;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiagnosticMethodFault() {
    }

    public DiagnosticMethodFault(DiagnosticMethod diagnosticMethod, FaultType faultType, CoverageLevel effectiveness) {
        this.diagnosticMethod = diagnosticMethod;
        this.faultType = faultType;
        this.effectiveness = effectiveness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagnosticMethodFault)) return false;
        DiagnosticMethodFault that = (DiagnosticMethodFault) o;
        return Objects.equals(diagnosticMethod, that.diagnosticMethod) &&
               Objects.equals(faultType, that.faultType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosticMethod, faultType);
    }
}
