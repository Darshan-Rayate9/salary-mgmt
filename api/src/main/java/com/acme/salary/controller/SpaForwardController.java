package com.acme.salary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Angular is served as static files by this same Spring Boot app. A hard refresh
 * or deep link to a client-side route (e.g. /employees/42) has no matching static
 * file, so without this it would 404 - forward those routes to the SPA shell
 * (index.html) and let the Angular router take over from there.
 *
 * Deliberately scoped to the known client routes rather than a catch-all, so it
 * never shadows /api/** (which must keep returning a real 404 for unmapped URLs).
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/employees", "/employees/**", "/dashboard", "/login"})
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
