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
public class CraSepdDatId implements Serializable {
    private String lotId;
    private String flow;
    private String oper;
    private String resId;
    private String tranTime;

    // getters and setters, equals, hashCode
}
