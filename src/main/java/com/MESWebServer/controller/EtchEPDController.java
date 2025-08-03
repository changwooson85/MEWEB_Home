package com.MESWebServer.controller;

import com.MESWebServer.DTO.EpdConditionRequest;
import com.MESWebServer.DTO.EpdResultDto;
import com.MESWebServer.DTO.MrasDwhDto;
import com.MESWebServer.DTO.ResDownConditionRequest;
import com.MESWebServer.DTO.ResEventInfo;
import com.MESWebServer.component.H101Service;
import com.MESWebServer.entity.Real.CraSepdDat;
import com.MESWebServer.h101.Core.MESWEBCaster;
import com.MESWebServer.h101.Core.MESWEBType;
import com.MESWebServer.repository.Real.CraSepdDatRepository;
import com.MESWebServer.repository.Real.MrasResdwhRepository;
import com.MESWebServer.service.CraspdDatService;
import com.MESWebServer.service.MrasdwhLstService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/etch/epd")
public class EtchEPDController {

    private final CraspdDatService crasepdDatService;
    private final MrasdwhLstService mrasdwhLstService;
    private final H101Service h101Service;

    @GetMapping("/conditions")
    @ResponseBody
    public Map<String, List<String>> getEtchConditions() {
        Map<String, List<String>> result = new HashMap<>();

        List<String> listLayer  = new ArrayList<>();
        listLayer.add(" ");
        listLayer.add("2ND POLY");
        listLayer.add("DPWELLM2");
        listLayer.add("FRONT METAL");
        listLayer.add("GATE");
        listLayer.add("METAL-1");
        listLayer.add("METAL-2");
        listLayer.add("METAL-3");
        listLayer.add("METAL-4");
        listLayer.add("METAL-5");
        listLayer.add("METAL-6");
        listLayer.add("MIM-2");
        listLayer.add("MIM-3");
        listLayer.add("MIM-4");
        listLayer.add("MIM-5");
        listLayer.add("MOAT");
        listLayer.add("P/O");
        listLayer.add("REV MOAT");
        listLayer.add("SAB");
        listLayer.add("TFR HEAD");
        listLayer.add("VIATOP");
        listLayer.add("ZDNWELL");

        List<String> listRedId  = new ArrayList<>();
        listRedId.add("ECGT01");
        listRedId.add("ECGT02");
        listRedId.add("ECGT03");
        listRedId.add("ECGT04");
        listRedId.add("ECGT05");
        listRedId.add("ECGT06");
        listRedId.add("ECGT07");
        listRedId.add("ECGT08");
        listRedId.add("ECGT09");
        listRedId.add("ECGT10");
        listRedId.add("ECTR01");
        listRedId.add("ECTR02");
        listRedId.add("ECTR03");
        listRedId.add("ECTR04");
        listRedId.add("EDFM01");
        listRedId.add("EDPS01");
        listRedId.add("EDPS02");
        listRedId.add("EDPS03");
        listRedId.add("EDPS04");
        listRedId.add("EDPS05");
        listRedId.add("EDPS06");
        listRedId.add("EDPS07");
        listRedId.add("EDPS08");
        listRedId.add("EDPS10");
        listRedId.add("EDPS11");
        listRedId.add("EDPS12");
        listRedId.add("EDPS13");
        listRedId.add("EDPS14");
        listRedId.add("EDPS15");
        listRedId.add("ERBO03");
        listRedId.add("ERBO04");
        listRedId.add("ERBO05");
        listRedId.add("ERBO06");
        listRedId.add("ERBO08");
        listRedId.add("ERBO09");
        listRedId.add("ERBO10");
        listRedId.add("ERBO11");
        listRedId.add("ERBO16");
        listRedId.add("ERBO17");
        listRedId.add("ERBO18");
        listRedId.add("ERBX01");
        listRedId.add("ERBX02");
        listRedId.add("ERBX03");
        listRedId.add("ERBX04");
        listRedId.add("ERBX05");
        listRedId.add("ERBX06");
        listRedId.add("ERBX07");
        listRedId.add("ETCP01");
        listRedId.add("ETCP02");
        listRedId.add("ETCP03");
        listRedId.add("ETCP04");
        listRedId.add("ETCP09");
        listRedId.add("ETCP10");
        listRedId.add("ETCP12");
        listRedId.add("ETCP13");


        List<String> listChamber  = new ArrayList<>();
        listChamber.add("A");
        listChamber.add("B");
        listChamber.add("C");
        listChamber.add("D");

        List<String> listRecipe  = new ArrayList<>();
        {
            MESWEBType.MESWEB_MGCMTBLDAT_In_Tag inTag = new MESWEBType.MESWEB_MGCMTBLDAT_In_Tag();
            MESWEBType.MESWEB_MGCMTBLDAT_Out_Tag outTag = new MESWEBType.MESWEB_MGCMTBLDAT_Out_Tag();
    
            inTag.h_factory ="AFB1";
            inTag.h_proc_step = '2';
            inTag.table_name ="ETC_EPD_COND";
            inTag.key_1 = "PPID";
            try{
                if (!h101Service.getIdleChannel().getM_mesWebCaster().MESWEB_View_MGCMTBLDAT(inTag, outTag)){
                    log.error("""
                    [ERROR] listRecipe
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                    );
                }
                for(int i = 0; i < outTag._size_data_list; i++){
                    listRecipe.add(outTag.data_list[i].key_2);
                }
            }catch (InterruptedException ex){
                log.error("""
                    [ERROR] listRecipe
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                );
            }

           /* if (!MESWEBCaster.MESWEB_View_MGCMTBLDAT(inTag, outTag)){
                log.error("""
                    [ERROR] listRecipe
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                );
                return null;
            }*/

        }

        List<String> listEpdStep  = new ArrayList<>();
        listEpdStep.add("PEP1");
        listEpdStep.add("PEP");
        listEpdStep.add("OE");
        listEpdStep.add("ME");
        listEpdStep.add("MAIN2");
        listEpdStep.add("MAIN ETCH");
        listEpdStep.add("MAIN");
        listEpdStep.add("BME");
        listEpdStep.add("BARC");
        listEpdStep.add("7");
        listEpdStep.add("6");
        listEpdStep.add("5");
        listEpdStep.add("4");
        listEpdStep.add("3");

        List<String> listEvent = new ArrayList<>();
        listEvent.add("PM");
        //listEvent.add("MONITOR");
        listEvent.add("AS PM");

        result.put("list_layer", listLayer);
        result.put("list_res_id", listRedId);
        result.put("list_chamber", listChamber);
        result.put("recipe", listRecipe);
        result.put("epdStep", listEpdStep);
        result.put("event", listEvent);

        // result.put("list_layer", crasepdDatService.getLayerList());
        // result.put("list_res_id", crasepdDatService.getEqList());
        // result.put("list_chamber", crasepdDatService.getChList());
        return result;
    }

    @PostMapping("/eq-downlist")
    @ResponseBody
    public ResponseEntity<List<MrasDwhDto>> getEqDownList(@RequestBody ResDownConditionRequest request){
        List<MrasDwhDto> results = mrasdwhLstService.findByCondition(request);
        return ResponseEntity.ok(results);
    }
    // @PostMapping("/etch-condition/data")
    // @ResponseBody
    // public List<Map<String, Object>> etchConditionData(
    //         @RequestBody Map<String, Object> conditions) {

    //     // 조건 기반 데이터 조회 로직 (예시)
    //     List<CraSepdDat> dataList = crasepdDatService.findByConditions(conditions);

    //     // 차트용 JSON 응답 준비
    //     return dataList.stream().map(d -> {
    //         Map<String, Object> map = new HashMap<>();
    //         map.put("time", d.getId().getTranTime());
    //         map.put("eq", d.getId().getResId());
    //         map.put("chamber", d.getChamber());
    //         map.put("epdTime", d.getEpdTime());
    //         return map;
    //     }).collect(Collectors.toList());
    // }


    @PostMapping("/etch-condition/data")
    @ResponseBody
    public ResponseEntity<?> etchConditionData(
        @RequestBody EpdConditionRequest request) {

            try {
                List<EpdResultDto> dataList = crasepdDatService.findByConditionsNative(request);
        
                List<Map<String, Object>> result = dataList.stream().map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("time", d.getTranTime());
                    map.put("eq", d.getResId());
                    map.put("chamber", d.getChamber());
                    map.put("epdTime", d.getEpdTime());
                    map.put("lot", d.getLotId());
                    map.put("slot", d.getSlot());
                    map.put("reticleDensity", d.getReticleDensity());
                    return map;
                }).collect(Collectors.toList());
                return ResponseEntity.ok(result); // 성공 응답
        
            } catch (Exception e) {
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("데이터 조회 중 오류가 발생했습니다.");
            }
    }

}
