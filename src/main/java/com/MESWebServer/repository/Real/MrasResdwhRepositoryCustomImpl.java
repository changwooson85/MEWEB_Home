package com.MESWebServer.repository.Real;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.MESWebServer.DTO.MrasDwhDto;
import com.MESWebServer.DTO.PeriodDto;
import com.MESWebServer.DTO.ResDownConditionRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MrasResdwhRepositoryCustomImpl implements MrasResdwhRepositoryCustom{
  private final EntityManager em;

  @Override
    public List<MrasDwhDto> findByDynamicCondition(ResDownConditionRequest request) {
      StringBuilder sql = new StringBuilder();
      sql.append("""
          SELECT FACTORY as factory, RES_ID as resId, DOWN_EVENT_ID as downEventId, DOWN_TRAN_TIME as downTranTime
          FROM MRASRESDWH
          WHERE 1=1
      """);

      Map<String, Object> params = new HashMap<>();

      if (request.getFactory() != null) {
        sql.append(" AND FACTORY = :factory ");
        params.put("factory", request.getFactory());
      }

      if (request.getEqPeriodMap() != null && !request.getEqPeriodMap().isEmpty()) {
        sql.append(" AND (");
        int idx = 0;
        for (Map.Entry<String, List<PeriodDto>> entry : request.getEqPeriodMap().entrySet()) {
            String eq = entry.getKey();
            List<PeriodDto> periods = entry.getValue();

            for (PeriodDto period : periods) {
                sql.append("(RES_ID = :eq").append(idx)
                   .append(" AND DOWN_TRAN_TIME BETWEEN :start").append(idx)
                   .append(" AND :end").append(idx).append(") OR ");

                params.put("eq" + idx, eq);
                params.put("start" + idx, period.getStartDate().replace("-", "") + "000000");
                params.put("end" + idx, period.getEndDate().replace("-", "") + "235959");
                idx++;
            }
        }
        sql.setLength(sql.length() - 4); // 마지막 OR 제거
        sql.append(") ");
    }

    if (request.getEventList() != null && !request.getEventList().isEmpty()) {
      sql.append(" AND (");
      int idx = 0;
      for(String event : request.getEventList()) {
        sql.append(" (DOWN_EVENT_ID = :event").append(idx).append(") OR ");
        params.put("event" + idx, event);
      }

      sql.setLength(sql.length() - 4); // 마지막 OR 제거
      sql.append(") ");
    }
    
    Query query = em.createNativeQuery(sql.toString());
    params.forEach(query::setParameter);
    List<Object[]> result = query.getResultList();

    return result.stream()
            .map(row -> new MrasDwhDto(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3]
            ))
            .collect(Collectors.toList());

    }
}
