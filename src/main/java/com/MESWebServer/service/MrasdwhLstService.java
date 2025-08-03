package com.MESWebServer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.MESWebServer.DTO.MrasDwhDto;
import com.MESWebServer.DTO.ResDownConditionRequest;
import com.MESWebServer.repository.Real.MrasResdwhRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MrasdwhLstService {
  private final MrasResdwhRepository repository;

    public List<MrasDwhDto> findByCondition(ResDownConditionRequest request) {
        return repository.findByDynamicCondition(request);
    }
}
