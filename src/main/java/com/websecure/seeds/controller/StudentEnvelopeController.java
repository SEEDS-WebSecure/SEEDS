package com.websecure.seeds.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/envelopes")
public class StudentEnvelopeController {
    @GetMapping
    public String studentEnvelopeHome() {
        return "student/searchDigitalEnvelopeForm";
    }

    // 조회 결과 화면
    @GetMapping("/results")
    public String searchDigitalEnvelopes(Model model) {
        // TODO: receiver 이름으로 전자봉투 조회 로직 추가 (현재는 테스트용 더미 데이터)
        List<Map<String, String>> envelopeList = new ArrayList<>();
        Map<String, String> envelope1 = new HashMap<>();
        envelope1.put("id", "1");
        envelope1.put("sender", "컨설턴트A");
        envelope1.put("receiver", "수험생B");
        envelope1.put("message", "첫 번째 메시지입니다.");
        envelope1.put("result", "검증 완료");
        envelopeList.add(envelope1);

        Map<String, String> envelope2 = new HashMap<>();
        envelope2.put("id", "2");
        envelope2.put("sender", "컨설턴트B");
        envelope2.put("receiver", "수험생B");
        envelope2.put("message", "두 번째 메시지입니다.");
        envelope2.put("result", "검증 대기");
        envelopeList.add(envelope2);

        model.addAttribute("envelopeList", envelopeList);
        return "student/searchDigitalEnvelopeForm";
    }


    // 상세 페이지: 전자봉투 하나 조회
    @GetMapping("/detail/{id}")
    public String viewEnvelopeDetail(@PathVariable Long id, Model model) {

        model.addAttribute("sender","컨설턴트A");
        model.addAttribute("receiver", "수험생B");
        model.addAttribute("message", "이것은 예시 메시지입니다.");
        model.addAttribute("result", "검증 완료");

        return "student/viewDetailDigitalEnvelope";
    }
}
