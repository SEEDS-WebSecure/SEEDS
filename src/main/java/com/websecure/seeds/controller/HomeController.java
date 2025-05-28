package com.websecure.seeds.controller;

import com.websecure.seeds.dto.CreateKeyDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping
    public String home(Model model) {
        model.addAttribute("createKeyDTO", new CreateKeyDTO());
        return "createKeyForm";
    }
}
