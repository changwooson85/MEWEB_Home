package com.MESWebServer.repository.Real;

import java.util.List;

import com.MESWebServer.DTO.MrasDwhDto;
import com.MESWebServer.DTO.ResDownConditionRequest;

public interface MrasResdwhRepositoryCustom {
  List<MrasDwhDto> findByDynamicCondition(ResDownConditionRequest request);
}
