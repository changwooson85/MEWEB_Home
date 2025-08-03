package com.MESWebServer.DTO;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResDownConditionRequest {
  private String factory;
  private Map<String, List<PeriodDto>> eqPeriodMap;
  private List<String> eventList;
}
