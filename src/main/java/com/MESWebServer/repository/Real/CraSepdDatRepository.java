package com.MESWebServer.repository.Real;

import com.MESWebServer.DTO.EpdConditionRequest;
import com.MESWebServer.entity.Real.CraSepdDat;
import com.MESWebServer.entity.Real.CraSepdDatId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CraSepdDatRepository extends JpaRepository<CraSepdDat, CraSepdDatId>
, JpaSpecificationExecutor<CraSepdDat> , CraSepdDatRepositoryCustom {

  @Query("SELECT DISTINCT e.reticleLayer FROM CraSepdDat e WHERE e.reticleLayer IS NOT NULL")
  List<String> findDistinctLayers();

  @Query("SELECT DISTINCT e.id.resId FROM CraSepdDat e WHERE e.id.resId IS NOT NULL")
  List<String> findDistinctResIds();

  @Query("SELECT DISTINCT e.chamber FROM CraSepdDat e WHERE e.chamber IS NOT NULL")
  List<String> findDistinctChambers();

}
