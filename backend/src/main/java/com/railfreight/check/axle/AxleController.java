package com.railfreight.check.axle;

import com.railfreight.check.axle.dto.AxleDetail;
import com.railfreight.check.axle.dto.AxleListItem;
import com.railfreight.check.axle.dto.CreateAxleRequest;
import com.railfreight.check.axle.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/axles")
public class AxleController {

    private final AxleService axleService;

    public AxleController(AxleService axleService) {
        this.axleService = axleService;
    }

    @GetMapping
    public List<AxleListItem> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return axleService.list(status, keyword);
    }

    @PostMapping("/create")
    public AxleDetail create(@Valid @RequestBody CreateAxleRequest req) {
        return axleService.create(req);
    }

    @GetMapping("/{axleId}")
    public AxleDetail detail(@PathVariable String axleId) {
        return axleService.detail(axleId);
    }

    @PutMapping("/{axleId}/status")
    public AxleDetail updateStatus(@PathVariable String axleId, @RequestBody UpdateStatusRequest req) {
        return axleService.updateStatus(axleId, req);
    }
}
