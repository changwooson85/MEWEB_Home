package com.MESWebServer.entity.Real;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class CrasJobLstId implements Serializable {
    private String factory;
    private String resId;
    private String device;
    private String layer;

}
