package com.MESWebServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeviceSearchRequest {
    private String device;
    private String res_id;
    private Integer size;
}
