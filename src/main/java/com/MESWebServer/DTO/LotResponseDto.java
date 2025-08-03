package com.MESWebServer.DTO;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LotResponseDto {
  private String lotId;
  private String flow;
  private String opn;
  private String location;
  private String recipe;
}
