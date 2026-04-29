package edu.mcw.rgd.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui.html")
    public String redirectLegacySwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}
