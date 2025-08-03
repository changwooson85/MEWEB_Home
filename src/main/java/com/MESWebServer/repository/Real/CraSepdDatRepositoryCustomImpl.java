package com.MESWebServer.repository.Real;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.MESWebServer.DTO.EpdConditionRequest;
import com.MESWebServer.DTO.EpdResultDto;
import com.MESWebServer.DTO.PeriodDto;
import com.MESWebServer.FunctionList.Common.Util.ResultMapper;
import com.MESWebServer.entity.Real.CraSepdDat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CraSepdDatRepositoryCustomImpl implements CraSepdDatRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<EpdResultDto> findByDynamicCondition(EpdConditionRequest request) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT
                   LOT_ID as lotId, SLOT as slot, RES_ID as resId, TRAN_TIME as tranTime, CHAMBER as chamber, RECIPE as recipe, EPD_STEP as epdStep, EPD_TIME as epdTime, RETICLE_DENSITY as reticleDensity, RETICLE_LAYER  as reticleLayer
            FROM CRASETCEPD
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();

        if (!CollectionUtils.isEmpty(request.getList_res_id())) {
            sql.append(" AND RES_ID IN (:listResId) ");
            params.put("listResId", request.getList_res_id());
        }
        
        if (!CollectionUtils.isEmpty(request.getPeriods())) {
            sql.append(" AND ( ");
            for (int i = 0; i < request.getPeriods().size(); i++) {
                PeriodDto p = request.getPeriods().get(i);
                String startKey = "startTime" + i;
                String endKey = "endTime" + i;

                sql.append(i == 0 ? "" : " OR ");
                sql.append(" TRAN_TIME BETWEEN :" + startKey + " AND :" + endKey + " ");

                params.put(startKey, p.getStartDate().replace("-", "") + "000000");
                params.put(endKey, p.getEndDate().replace("-", "") + "235959");
            }
            sql.append(" ) ");
        }

        if (!CollectionUtils.isEmpty(request.getLayer())) {
            sql.append(" AND RETICLE_LAYER IN (:layer) ");
            params.put("layer", request.getLayer());
        }

        if (!CollectionUtils.isEmpty(request.getChamber())) {
            sql.append(" AND CHAMBER IN (:chamber) ");
            params.put("chamber", request.getChamber());
        }

        if (!CollectionUtils.isEmpty(request.getRecipe())) {
            sql.append(" AND PPID IN (:recipe) ");
            params.put("recipe", request.getRecipe());
        }

        if (!CollectionUtils.isEmpty(request.getEpdStep())) {
            sql.append(" AND EPD_STEP IN (:epdStep) ");
            params.put("epdStep", request.getEpdStep());
        }

        if (StringUtils.hasText(request.getDevice())) {
            sql.append(" AND DEV = :device ");
            params.put("device", request.getDevice());
        }

        if (StringUtils.hasText(request.getRoute())) {
            sql.append(" AND FLOW LIKE '%' || :route ");
            params.put("route", request.getRoute());
        }

        if (StringUtils.hasText(request.getProcess())) {
            sql.append(" AND OPER = :process ");
            params.put("process", request.getProcess());
        }

        if (StringUtils.hasText(request.getLsl())) {
            sql.append(" AND RETICLE_DENSITY >= :lsl ");
            params.put("lsl", new BigDecimal(request.getLsl()));
        }

        if (StringUtils.hasText(request.getUsl())) {
            sql.append(" AND RETICLE_DENSITY <= :usl ");
            params.put("usl", new BigDecimal(request.getUsl()));
        }


        Query query = em.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        
        List<Object[]> result = query.getResultList();
        
        return result.stream()
            .map(row -> new EpdResultDto(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (String) row[5],
                (String) row[6],
                ResultMapper.toDouble(row[7]),
                ResultMapper.toDouble(row[8]),
                (String) row[9]
            ))
            .collect(Collectors.toList());
    }
}
