package com.MESWebServer.entity.Real;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "CRASETCEPD")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CraSepdDat {

    @EmbeddedId // ✅ 복합 키로 설정
    private CraSepdDatId id;

    @Column(name = "MAT_ID", length = 30, nullable = false)
    private String matId;
    @Column(name = "DEV", length = 30, nullable = false)
    private String dev;
    @Column(name = "CHAMBER", length = 25, nullable = false)
    private String chamber;
    @Column(name = "SLOT", length = 25, nullable = false)
    private String slot;
    @Column(name = "RECIPE", length = 25, nullable = false)
    private String recipe;
    @Column(name = "PPID", length = 25, nullable = false)
    private String ppid;
    @Column(name = "EPD_STEP", length = 25, nullable = false)
    private String epdStep;
    @Column(name = "EPD_TIME")
    private Double epdTime;
    @Column(name = "RETICLE_DENSITY")
    private Double reticleDensity;
    @Column(name = "RF_TIME")
    private Double rfTime;
    @Column(name = "TURBO_TIME")
    private Double turboTime;
    @Column(name = "RETICLE_LAYER", length = 30, nullable = false)
    private String reticleLayer;

    // getters and setters
}