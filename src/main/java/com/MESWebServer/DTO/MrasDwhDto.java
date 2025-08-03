package com.MESWebServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MrasDwhDto {
    private String factory;
    private String resId;
    private String downEventId;
    private String downTranTime;
}