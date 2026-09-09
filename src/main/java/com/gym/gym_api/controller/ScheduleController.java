package com.gym.gym_api.controller;

import com.gym.gym_api.dto.workout.schedule.ScheduleRequest;
import com.gym.gym_api.dto.workout.schedule.ScheduleResponse;
import com.gym.gym_api.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(
            ScheduleService scheduleService
    ) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleResponse> getAllSchedules(
            Authentication authentication
    ) {
        return scheduleService.getAllSchedules(authentication);
    }

    @GetMapping("/{id}")
    public ScheduleResponse getScheduleById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return scheduleService.getScheduleById(
                id,
                authentication
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createSchedule(
            @Valid @RequestBody ScheduleRequest request,
            Authentication authentication
    ) {
        return scheduleService.createSchedule(
                request,
                authentication
        );
    }

    @PutMapping("/{id}")
    public ScheduleResponse updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request,
            Authentication authentication
    ) {
        return scheduleService.updateSchedule(
                id,
                request,
                authentication
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(
            @PathVariable Long id,
            Authentication authentication
    ) {
        scheduleService.deleteSchedule(
                id,
                authentication
        );
    }
}