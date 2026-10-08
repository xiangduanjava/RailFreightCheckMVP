package com.railfreight.check.detection;

import com.railfreight.check.axle.dto.AxleDetail;
import com.railfreight.check.detection.dto.CreateDetectionRequest;
import com.railfreight.check.detection.dto.DetectionItem;
import com.railfreight.check.detection.dto.DetectionSummary;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping
    public AxleDetail create(@Valid @RequestBody CreateDetectionRequest req) {
        return detectionService.create(req);
    }

    @GetMapping
    public List<DetectionItem> list(@RequestParam(name = "limit", defaultValue = "5") int limit) {
        return detectionService.list(limit);
    }

    @GetMapping("/summary")
    public DetectionSummary summary() {
        return detectionService.summary();
    }
}
