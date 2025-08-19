package com.MESWebServer.controller;

import com.MESWebServer.DTO.LotResponseDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(@RequestParam(required = false) String pgedit,
        @RequestParam(required = false) String pgpvvid) {
        // Pinegrow가 보내는 파라미터를 받아내지만, 실제 로직에는 사용하지 않음
        return "home";
    }
    @GetMapping("/line")
    public String getLineSheduler(@RequestParam(value="res_id", required = true) String res_id, Model model){

        List<LotResponseDto> lotList = fetchLotList(); // 서비스나 DAO에서 조회
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("queryTime", now.format(formatter));
        model.addAttribute("res_id", "TEST");
        model.addAttribute("lotList", lotList);

        return "fragments/lot_table";
    }

    private List<LotResponseDto> fetchLotList() {
        List<LotResponseDto> list = new ArrayList<>();
        list.add(LotResponseDto.builder()
            .lotId("LOT001").flow("FLOW1").opn("OP10").location("EQ01-CH123").recipe("RECIPE_X").build());
        list.add(LotResponseDto.builder()
            .lotId("LOT002").flow("FLOW2").opn("OP10").location("EQ01-CH2").recipe("RECIPE_Y").build());
        return list;
    }
}
