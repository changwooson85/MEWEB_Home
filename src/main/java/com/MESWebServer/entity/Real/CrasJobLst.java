package com.MESWebServer.entity.Real;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name ="CRASJOBLST")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrasJobLst {

/*
    @Id
    @Column(name = "RES_ID", length = 20, nullable = false)
    private String resId;

    @Id
    @Column(name = "DEVICE", length = 20, nullable = false)
    private String device;

    @Id
    @Column(name = "LAYER", length = 20, nullable = false)
    private String layer;
*/

    @EmbeddedId // ✅ 복합 키로 설정
    private CrasJobLstId id;

    @Column(name = "LAST_MODIFY_TIME", length = 14, nullable = false)
    private String lastModifyTime;

    @Column(name = "UPDATE_TIME", length = 14, nullable = false)
    private String updateTime;

    @Column(name = "DELETE_FLAG", length = 1, nullable = false)
    private String deleteFlag;

    @Column(name = "CMF_1", length = 50, nullable = false)
    private String cmf_1;

    @Column(name = "CMF_2", length = 50, nullable = false)
    private String cmf_2;

    @Column(name = "CMF_3", length = 50, nullable = false)
    private String cmf_3;

    @Column(name = "CMF_4", length = 50, nullable = false)
    private String cmf_4;

    @Column(name = "CMF_5", length = 50, nullable = false)
    private String cmf_5;
}
