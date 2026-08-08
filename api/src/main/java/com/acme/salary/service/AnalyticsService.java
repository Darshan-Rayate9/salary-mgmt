package com.acme.salary.service;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.DistributionBucketResponse;
import com.acme.salary.dto.GroupAggregateResponse;
import com.acme.salary.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsSummaryResponse summary() {
        return analyticsRepository.summary();
    }

    public List<GroupAggregateResponse> byDepartment() {
        return analyticsRepository.byDepartment();
    }

    public List<GroupAggregateResponse> byCountry() {
        return analyticsRepository.byCountry();
    }

    public List<GroupAggregateResponse> byLevel() {
        return analyticsRepository.byLevel();
    }

    public List<DistributionBucketResponse> distribution() {
        return analyticsRepository.distribution();
    }
}
