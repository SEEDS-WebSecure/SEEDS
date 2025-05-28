package com.websecure.seeds.controller;

import com.websecure.seeds.domain.User;
import com.websecure.seeds.dto.CreateKeyDTO;
import com.websecure.seeds.service.KeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/keys")
public class KeyController {
    private final KeyService keyService;

    @GetMapping("")
    public String createKey(Model model) {
        model.addAttribute("createKeyDTO", new CreateKeyDTO());
        return "createKeyForm";
    }

    @PostMapping("")
    public String createKey(@ModelAttribute("createKeyDTO") CreateKeyDTO request, Model model, BindingResult result) {
        if(result.hasErrors()) {
            return "createKeyForm";
        }

        User user = keyService.createKey(request);

        boolean isSuccess = (user != null);
        model.addAttribute("isSuccess", isSuccess);

        return "createKeyForm";
    }
}
