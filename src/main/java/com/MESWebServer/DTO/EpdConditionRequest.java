package com.MESWebServer.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EpdConditionRequest {
  private List<String> layer;
  private List<String> list_res_id;
  private List<PeriodDto> periods;
  private List<String> chamber;
  private List<String> recipe;
  private List<String> epdStep;

  private String device;
  private String route;
  private String process;
  private String usl;
  private String lsl;

}
