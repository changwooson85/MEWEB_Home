package com.MESWebServer.controller;

import com.MESWebServer.DTO.DeviceSearchRequest;
import com.MESWebServer.FunctionList.FTP.FtpCheckType;
import com.MESWebServer.FunctionList.FTP.FtpServer;
import com.MESWebServer.component.H101Service;
import com.MESWebServer.entity.Real.CrasJobLst;
import com.MESWebServer.h101.Core.MESWEBCaster;
import com.MESWebServer.h101.Core.MESWEBType;
import com.MESWebServer.service.CrasJobLstService;
import com.MESWebServer.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RequestMapping("/crasjoblst")
@Controller
@Slf4j
public class CrasJobLstController{
    private final CrasJobLstService service;
    private final ExcelService excelService; // 엑셀 생성 서비스
    private final H101Service h101Service;

    public CrasJobLstController(CrasJobLstService service, ExcelService excelService, H101Service h101Service) {
        this.service = service;
        this.excelService = excelService;
        this.h101Service = h101Service;
    }

    // 10만 건 데이터 생성 API
    /*@PostMapping("/generate")
    public String generateData() {
        service.generateAndSaveData(100000);
        return "100,000 records inserted successfully!";
    }*/

    @GetMapping("/res_id")
    @ResponseBody
    public List<String> listCrasJobLst(Model model){

        MESWEBType.MESWEB_MGCMTBLDAT_In_Tag inTag = new MESWEBType.MESWEB_MGCMTBLDAT_In_Tag();
        MESWEBType.MESWEB_MGCMTBLDAT_Out_Tag outTag = new MESWEBType.MESWEB_MGCMTBLDAT_Out_Tag();

        inTag.h_factory ="AFB1";
        inTag.h_proc_step = '1';
        inTag.table_name ="PHOTO_FTP_JOB_FILE";

        try {
            if (!h101Service.getIdleChannel().getM_mesWebCaster().MESWEB_View_MGCMTBLDAT(inTag, outTag)) {
                log.error("""
                    [ERROR] listCrasJobLst
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                );
                return null;
            }
/*                if (!MESWEBCaster.MESWEB_View_MGCMTBLDAT(inTag, outTag)){
                    log.error("""
                    [ERROR] listCrasJobLst
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                    );
                    return null;
                }*/
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> dataList = new ArrayList<>();
        for(int i = 0; i < outTag._size_data_list; i++){
            dataList.add(outTag.data_list[i].key_1);
        }
        model.addAttribute("res_id", dataList);
        return dataList;
    }
    @GetMapping("/view")
    public String listCrasJobLst(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "res_id",required = true) String res_id,
            @RequestParam(value = "device", required = false) String device) {


        //Page<CrasJobLst> crasJobLstPage = service.getCrasJobLstPage11g(res_id, page, size);
        Page<CrasJobLst> crasJobLstPage;
        if (device != null && !device.isEmpty()){
            crasJobLstPage = service.getResIdAndDevicePage(res_id, device, page, size);
        }
        else {
            crasJobLstPage = service.getRedIdCrasJobLstPage(res_id, page, size);
        }
        // 현재 페이지를 기준으로 10개씩 페이지 번호 계산
        int currentPage = crasJobLstPage.getNumber();
        int totalPages = crasJobLstPage.getTotalPages();

        // 10개의 페이지 번호를 표시하려면
        int startPage = (currentPage / 10) * 10;  // 앞의 페이지 번호
        int endPage = Math.min(startPage + 9, totalPages - 1);  // 뒤의 페이지 번호 (최대 10개)


        model.addAttribute("res_id", res_id);
        model.addAttribute("device", device);
        model.addAttribute("crasJobLstPage", crasJobLstPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "fragments/crasjoblst_list::jobListContent";
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadExcel(@RequestParam(value = "res_id",required = false) String res_id) throws IOException {
        // 모든 데이터를 조회

        List<CrasJobLst> crasJobLstList;
        if (res_id == null){
            crasJobLstList = service.findAll();
        }else{
            crasJobLstList = service.findResId(res_id);
        }

        // 엑셀 파일 생성
        ByteArrayOutputStream excelFile = excelService.generateExcel(crasJobLstList);

        // ByteArrayResource로 파일 반환
        ByteArrayResource resource = new ByteArrayResource(excelFile.toByteArray());

        // HTTP 응답으로 엑셀 파일 다운로드 처리
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+ res_id +"_job_list.xlsx")
                .body(resource);
    }

/*    // ✅ Controller - 검색 시 해당 페이지로 리다이렉트
    //@GetMapping("/search")
    @PostMapping("/search")
    @ResponseBody
    public Map<Integer, Integer> searchDeviceRedirect(
            @RequestParam("device") String device,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "res_id", required = true) String resId,
            RedirectAttributes redirectAttributes
    ) {
        Integer page = service.findPageNumberForDevice(resId, device, size);

        Map<Integer, Integer> result = new HashMap<>();
        if (page == null || page < 0) {
            redirectAttributes.addFlashAttribute("message", "해당 DEVICE를 찾을 수 없습니다.");
            return result;
        }
        //return "redirect:/crasjoblst/view?page=" + page + "&size=" + size + "&res_id=" + resId;
        result.put(page, size);
        return result;
    }*/
    // ✅ Controller - 검색 시 해당 페이지로 리다이렉트
    @PostMapping("/search")
    @ResponseBody
    public Map<String, Object> searchDeviceRedirect(
            @RequestBody DeviceSearchRequest request
    ) {
        String device = request.getDevice();
        String resId = request.getRes_id();
        int size = request.getSize() != null ? request.getSize() : 20;
        Map<String, Object> result = new HashMap<>();
        try {

            Integer page = service.findPageNumberForDevice(resId, device, size);

            if (page == null || page < 0) {
                result.put("success", false);
                result.put("message", "해당 DEVICE를 찾을 수 없습니다.");
            } else {
                result.put("success", true);
                result.put("page", page);
                result.put("device", device);
            }
        }catch(Exception ex){
            result.put("success", false);
            result.put("message", "해당 DEVICE를 찾을 수 없습니다.");
        }

        return result;
    }
    @GetMapping("/test")
    public String testPage() {
        return "test"; // test.html 템플릿 파일을 반환
    }
}
