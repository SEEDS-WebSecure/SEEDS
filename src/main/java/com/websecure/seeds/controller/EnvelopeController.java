package com.websecure.seeds.controller;

import com.websecure.seeds.domain.Envelope;
import com.websecure.seeds.domain.VerifySignDTO;
import com.websecure.seeds.dto.SendEnvelopeDTO;
import com.websecure.seeds.service.EnvelopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/envelopes")
public class EnvelopeController {
    private final EnvelopeService envelopeService;

    @GetMapping("/consultant")
    public String sendDigitalEnvelope(Model model) {
        model.addAttribute("sendEnvelopeDTO", new SendEnvelopeDTO());
        return "sendDigitalEnvelopeForm";
    }

    @PostMapping("/consultant")
    public String sendDigitalEnvelope(@ModelAttribute("sendEnvelopeDTO") SendEnvelopeDTO request, Model model, BindingResult result) {
        if(result.hasErrors()) {
            return "sendDigitalEnvelopeForm";
        }

        Envelope envelope = envelopeService.sendDigitalEnvelope(request);

        boolean isSuccess = (envelope != null);
        model.addAttribute("isSuccess", isSuccess);

        return "sendDigitalEnvelopeForm";
    }

    @GetMapping("/student")
    public String studentEnvelopeHome() {
        return "student/searchDigitalEnvelopeForm";
    }

    // 조회 결과 화면
    @GetMapping("student/results")
    public String searchDigitalEnvelopes(@RequestParam String receiver, Model model) {
        List<Envelope> envelopeList = envelopeService.findEnvelopeList(receiver);

        model.addAttribute("envelopeList", envelopeList);
        return "student/searchDigitalEnvelopeForm";
    }


    // 상세 페이지: 전자봉투 하나 조회
    @GetMapping("student/results/{envelopeId}/detail")
    public String viewEnvelopeDetail(@PathVariable Long envelopeId, @RequestParam String receiver, Model model) {
        VerifySignDTO verifySignDTO =  envelopeService.verifySign(envelopeId,receiver);
        model.addAttribute("verifySignDTO",verifySignDTO);
        return "student/viewDetailDigitalEnvelope";
    }
}
