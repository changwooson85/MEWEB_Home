package com.MESWebServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EpdResultDto {
  private String lotId;
  private String slot;
  private String resId;
  private String tranTime;
  private String chamber;
  private String recipe;
  private String epdStep;
  private Double epdTime;
  private Double reticleDensity;
  private String reticleLayer;
}
