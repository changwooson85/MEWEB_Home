package com.MESWebServer.repository.Real;


import com.MESWebServer.entity.Real.CrasJobLst;
import com.MESWebServer.entity.Real.CrasJobLstId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrasJobLstRepository extends JpaRepository<CrasJobLst, CrasJobLstId> {

    //Oracle 12g에서 정상 사용 가능
    List<CrasJobLst> findByIdResId(String resId);
    Page<CrasJobLst> findAll(Pageable pageable);
    Page<CrasJobLst> findByIdResId(String resId, Pageable pageable);
    Page<CrasJobLst> findByIdResIdAndIdDevice(String resId, String device, Pageable pageable);
    void deleteByIdFactoryAndIdResId(String factory, String resId);
    @Query(value = "SELECT * FROM ( " +
            "SELECT a.*, ROWNUM rnum FROM ( " +
            "SELECT * FROM CRASJOBLST WHERE RES_ID = :resId ORDER BY RES_ID " +
            ") a WHERE ROWNUM <= :endRow " +
            ") WHERE rnum > :startRow", nativeQuery = true)
    List<CrasJobLst> findPagedByResId(
            @Param("resId") String resId,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    @Query(value = "SELECT COUNT(*) FROM CRASJOBLST WHERE RES_ID = :resId", nativeQuery = true)
    long countByResId(@Param("resId") String resId);

    @Query(value = "SELECT CEIL(rownum / :pageSize) - 1 AS page_num FROM ( " +
            "SELECT device FROM CRASJOBLST WHERE res_id = :resId ORDER BY device ASC, layer ASC " +
            ") WHERE device = :device AND rownum = 1", nativeQuery = true)
    Integer findPageNumberForDevice11g(@Param("resId") String resId, @Param("device") String device, @Param("pageSize") int pageSize);

        @Query(value = """
    SELECT FLOOR((page_no - 1) / :pageSize) FROM (
        SELECT device,
               ROW_NUMBER() OVER (ORDER BY device ASC, layer ASC) AS page_no
        FROM CRASJOBLST
        WHERE res_id = :resId
    ) WHERE device = :device AND rownum = 1
    """, nativeQuery = true)
        Integer findPageNumberForDevice(@Param("resId") String resId, @Param("device") String device, @Param("pageSize") int pageSize);

        @Modifying
        @Query(value= "DELETE FROM CRASJOBLST WHERE FACTORY = :factory AND RES_ID=:resId", nativeQuery = true)
        void deleteNativeByFactoryAndResId(@Param("factory") String factory, @Param("resId") String resId);

}
