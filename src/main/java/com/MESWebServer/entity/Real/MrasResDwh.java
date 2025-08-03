package com.MESWebServer.entity.Real;

import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Immutable  // 읽기 전용 처리
@Table(name="MRASRESDWH")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MrasResDwh {
    @Id
    @Column(name = "ROWID")
    private String rowid; // MVIEW에는 PK가 없으므로 ROWID 사용
    @Column(name = "FACTORY")
    private String factory;
    @Column(name = "RES_ID")
    private String resId;
    @Column(name = "DOWN_EVENT_ID")
    private String downEventId;
    @Column(name = "DOWN_TRAN_TIME")
    private String downTranTime;
}
