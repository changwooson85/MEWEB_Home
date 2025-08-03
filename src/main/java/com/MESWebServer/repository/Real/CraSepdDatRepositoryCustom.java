package com.MESWebServer.repository.Real;

import java.util.List;

import com.MESWebServer.DTO.EpdConditionRequest;
import com.MESWebServer.DTO.EpdResultDto;
import com.MESWebServer.entity.Real.CraSepdDat;

public interface CraSepdDatRepositoryCustom {
  List<EpdResultDto> findByDynamicCondition(EpdConditionRequest request);
}