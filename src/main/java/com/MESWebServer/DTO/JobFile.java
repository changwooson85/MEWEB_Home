package com.MESWebServer.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JobFile {
    private String device;
    private String layer;
    private String timeStamp;
}
