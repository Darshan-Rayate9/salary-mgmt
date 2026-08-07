package com.acme.salary.controller;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.DistributionBucketResponse;
import com.acme.salary.dto.GroupAggregateResponse;
import com.acme.salary.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary() {
        return analyticsService.summary();
    }

    @GetMapping("/by-department")
    public List<GroupAggregateResponse> byDepartment() {
        return analyticsService.byDepartment();
    }

    @GetMapping("/by-country")
    public List<GroupAggregateResponse> byCountry() {
        return analyticsService.byCountry();
    }

    @GetMapping("/by-level")
    public List<GroupAggregateResponse> byLevel() {
        return analyticsService.byLevel();
    }

    @GetMapping("/distribution")
    public List<DistributionBucketResponse> distribution() {
        return analyticsService.distribution();
    }
}
