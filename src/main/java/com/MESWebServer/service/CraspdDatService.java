package com.MESWebServer.service;


import com.MESWebServer.DTO.EpdConditionRequest;
import com.MESWebServer.DTO.EpdResultDto;
import com.MESWebServer.DTO.PeriodDto;
import com.MESWebServer.entity.Real.CraSepdDat;
import com.MESWebServer.repository.Real.CraSepdDatRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CraspdDatService {

    private final CraSepdDatRepository repository;

    public List<String> getLayerList() {
        return repository.findDistinctLayers();
    }

    public List<String> getEqList() {
        return repository.findDistinctResIds();
    }

    public List<String> getChList() {
        return repository.findDistinctChambers();
    }

    public List<CraSepdDat> findByConditions(Map<String, Object> conditions) {
        return repository.findAll(createSpecification(conditions));
    }

    
    public List<CraSepdDat> findByConditions(EpdConditionRequest conditions) {
        return repository.findAll(createSpecification(conditions));
    }
    public List<EpdResultDto> findByConditionsNative(EpdConditionRequest conditions) {
        return repository.findByDynamicCondition(conditions);
    }
    private Specification<CraSepdDat> createSpecification(Map<String, Object> conditions) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Layer 조건
            if (conditions.get("layer") != null) {
                List<String> layers = asList(conditions.get("layer"));
                predicates.add(root.get("reticleLayer").in(layers));
            }

            // 장비(EQ) 조건 (list_res_id)
            if (conditions.get("list_res_id") != null) {
                List<String> resIds = asList(conditions.get("list_res_id"));
                predicates.add(root.get("id").get("resId").in(resIds));
            }

            // Chamber 조건
            if (conditions.get("ch") != null) {
                List<String> chambers = asList(conditions.get("ch"));
                predicates.add(root.get("chamber").in(chambers));
            }

            // 기간 조건 (startDate, endDate) : 여러 기간이 OR 조건으로 처리됨
            if (conditions.get("startDate[]") != null && conditions.get("endDate[]") != null) {
                List<String> startDates = asList(conditions.get("startDate[]"));
                List<String> endDates = asList(conditions.get("endDate[]"));

                List<Predicate> datePredicates = new ArrayList<>();
                for (int i = 0; i < startDates.size(); i++) {
                    // 날짜 형식: yyyy-MM-dd → yyyyMMddHHmmss (시작은 000000, 종료는 235959로 확장)
                    String start = startDates.get(i).replace("-", "") + "000000";
                    String end = endDates.get(i).replace("-", "") + "235959";
                    datePredicates.add(cb.between(root.get("id").get("tranTime"), start, end));
                }
                predicates.add(cb.or(datePredicates.toArray(new Predicate[0])));
            }

            // 필요시 추가 조건들 (lot, slot, route, process, device, use, lsl 등)도 이곳에 추가

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<CraSepdDat> createSpecification(EpdConditionRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Layer 조건trxmfos
            // Layer 조건
            if (request.getLayer() != null && !request.getLayer().isEmpty()) {
                predicates.add(root.get("reticleLayer").in(request.getLayer()));
            }

            // EQ 조건
            if (request.getList_res_id() != null && !request.getList_res_id().isEmpty()) {
                predicates.add(root.get("id").get("resId").in(request.getList_res_id()));
            }
    
            // Chamber 조건
            if (request.getChamber() != null && !request.getChamber().isEmpty()) {
                predicates.add(root.get("chamber").in(request.getChamber()));
            }
    

            // Period (startDate ~ endDate) 여러개 OR 처리
        if (request.getPeriods() != null && !request.getPeriods().isEmpty()) {
            List<Predicate> datePredicates = new ArrayList<>();

            for (PeriodDto p : request.getPeriods()) {
                String start = p.getStartDate().replace("-", "") + "000000";
                String end = p.getEndDate().replace("-", "") + "235959";
                datePredicates.add(cb.between(root.get("id").get("tranTime"), start, end));
            }
            predicates.add(cb.or(datePredicates.toArray(new Predicate[0])));
        }

        // Device
        if (StringUtils.hasText(request.getDevice())) {
            predicates.add(cb.equal(root.get("dev"), request.getDevice()));
        }

        // Route
        if (StringUtils.hasText(request.getRoute())) {
            predicates.add(cb.equal(root.get("flow"), request.getRoute()));
        }

        // Process
        if (StringUtils.hasText(request.getProcess())) {
            predicates.add(cb.equal(root.get("oper"), request.getProcess()));
        }

        // LSL
        if (StringUtils.hasText(request.getLsl())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("epdTime"), new BigDecimal(request.getLsl())));
        }

        // USL
        if (StringUtils.hasText(request.getUsl())) {
            predicates.add(cb.lessThanOrEqualTo(root.get("epdTime"), new BigDecimal(request.getUsl())));
        }

            // 필요시 추가 조건들 (lot, slot, route, process, device, use, lsl 등)도 이곳에 추가

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 입력이 단일 문자열 또는 리스트 형태 모두 지원하도록 처리
    private List<String> asList(Object obj) {
        if (obj instanceof List) {
            return (List<String>) obj;
        }
        return Collections.singletonList(obj.toString());
    }

}
