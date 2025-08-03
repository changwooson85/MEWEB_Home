package com.MESWebServer.repository.Real;

import org.springframework.data.jpa.repository.JpaRepository;

import com.MESWebServer.entity.Real.MrasResDwh;
public interface MrasResdwhRepository extends JpaRepository<MrasResDwh, String>, MrasResdwhRepositoryCustom {
  
}
