package com.MESWebServer.service;


import com.MESWebServer.entity.Real.CrasJobLst;
import com.MESWebServer.entity.Real.CrasJobLstId;
import com.MESWebServer.repository.Real.CrasJobLstRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class CrasJobLstService {

    private final CrasJobLstRepository repository;

    @PersistenceContext
    private EntityManager em;

    public CrasJobLstService(CrasJobLstRepository repository) {
        this.repository = repository;
    }


    private static final Random RANDOM = new Random();

    @Transactional
    public void generateAndSaveData(int count) {
        List<CrasJobLst> dataList = IntStream.range(0, count)
                .mapToObj(i -> createRandomData(i))
                .collect(Collectors.toList());

        repository.saveAll(dataList);
    }

    private CrasJobLst createRandomData(int index) {
        CrasJobLstId id = new CrasJobLstId(
                "AFB1",
                "PSTD20",
                "DEV" + (index % 500000),
                "LAYER" + (index % 200000)
        );

        return CrasJobLst.builder()
                .id(id)
                //.updateTime(generateRandomTime())
                .build();
    }

    // 랜덤한 시간 생성
    private String generateRandomTime() {
        LocalDateTime now = LocalDateTime.now().minusDays(RANDOM.nextInt(30)); // 최근 30일 내 데이터
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public Page<CrasJobLst> getCrasJobLstPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("id.resId", "id.device", "id.layer")));
    }

    public Page<CrasJobLst> getRedIdCrasJobLstPage(String resId, int page, int size) {
        return repository.findByIdResId(resId, PageRequest.of(page, size, Sort.by("id.resId", "id.device", "id.layer")));
    }

    public Page<CrasJobLst> getCrasJobLstPage11g(String resId, int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;

        List<CrasJobLst> content = repository.findPagedByResId(resId, startRow, endRow);
        long total = repository.countByResId(resId);

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total);
    }

    public List<CrasJobLst> findAll() {
        return repository.findAll();
    }

    public List<CrasJobLst> findResId(String resId) {
        return repository.findByIdResId(resId);
    }

    public Integer findPageNumberForDevice(String resId, String device, int size) {
        return repository.findPageNumberForDevice(resId, device, size);
    }
    public Page<CrasJobLst> getResIdAndDevicePage(String resId, String device, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id.device").ascending().and(Sort.by("id.layer").ascending()));
        return repository.findByIdResIdAndIdDevice(resId, device, pageable);
    }
    @Transactional
    public void refreshData(String factory, String resId, List<CrasJobLst> newList){
        repository.deleteNativeByFactoryAndResId(factory, resId);
        for(int i = 0; i < newList.size(); i++){
            if (null == newList.get(i).getId().getDevice() || newList.get(i).getId().getDevice().isEmpty()
                    || null == newList.get(i).getId().getLayer() || newList.get(i).getId().getLayer().isEmpty())
            {
                log.error("resId:{} device:{} layer:{}",resId, newList.get(i).getId().getDevice(), newList.get(i).getId().getLayer());
                continue;
            }
            em.merge(newList.get(i));

            if(i % 500 == 0){
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();
    }
}
