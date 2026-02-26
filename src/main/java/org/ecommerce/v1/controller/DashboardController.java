package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.ResponseDto.DashboardStatsDTO;
import org.ecommerce.v1.dto.ResponseDto.EndpointMetricsDTO;
import org.ecommerce.v1.metrics.EndpointMetricsStore;
import org.ecommerce.v1.service.DashboardService;
import org.ecommerce.v1.service.RecentActivityBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated stats (async, parallel)")
public class DashboardController {

    private final DashboardService dashboardService;
    private final RecentActivityBuffer recentActivityBuffer;
    private final EndpointMetricsStore endpointMetricsStore;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<DashboardStatsDTO>> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return ResponseEntity.ok(new SuccessResponse<>("Dashboard stats", stats));
    }

    @GetMapping("/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<List<RecentActivityBuffer.ActivityEntry>>> getRecentActivity(
            @RequestParam(defaultValue = "50") int limit) {
        List<RecentActivityBuffer.ActivityEntry> activity = recentActivityBuffer.getRecent(limit);
        return ResponseEntity.ok(new SuccessResponse<>("Recent activity", activity));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<List<EndpointMetricsDTO>>> getMetrics() {
        List<EndpointMetricsDTO> list = endpointMetricsStore.snapshot().entrySet().stream()
                .map(e -> EndpointMetricsDTO.builder()
                        .endpoint(e.getKey())
                        .count(e.getValue().count())
                        .avgMs(e.getValue().avgMs())
                        .maxMs(e.getValue().maxMs())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SuccessResponse<>("Endpoint metrics", list));
    }
}
