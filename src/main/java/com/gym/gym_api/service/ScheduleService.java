package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.schedule.ScheduleRequest;
import com.gym.gym_api.dto.workout.schedule.ScheduleResponse;
import com.gym.gym_api.entity.Schedule;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ScheduleRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            WorkoutTemplateRepository workoutTemplateRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.workoutTemplateRepository = workoutTemplateRepository;
    }

    public List<ScheduleResponse> getAllSchedules(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return scheduleRepository.findAll()
                .stream()
                .filter(schedule ->
                        schedule.getUser().getId().equals(user.getId())
                )
                .map(this::toResponse)
                .toList();
    }

    public ScheduleResponse getScheduleById(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found: " + id
                        )
                );

        verifyOwnership(schedule, user);

        return toResponse(schedule);
    }

    public ScheduleResponse createSchedule(
            ScheduleRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template =
                workoutTemplateRepository.findById(
                        request.getWorkoutTemplateId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout template not found: "
                                        + request.getWorkoutTemplateId()
                        )
                );

        verifyTemplateOwnership(template, user);

        Schedule schedule = new Schedule();

        schedule.setUser(user);
        schedule.setWorkoutTemplate(template);
        schedule.setScheduledDate(request.getScheduledDate());
        schedule.setScheduledTime(request.getScheduledTime());
        schedule.setNotes(request.getNotes());
        schedule.setCreatedAt(LocalDateTime.now());

        Schedule savedSchedule =
                scheduleRepository.save(schedule);

        return toResponse(savedSchedule);
    }

    public ScheduleResponse updateSchedule(
            Long id,
            ScheduleRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found: " + id
                        )
                );

        verifyOwnership(schedule, user);

        WorkoutTemplate template =
                workoutTemplateRepository.findById(
                        request.getWorkoutTemplateId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout template not found: "
                                        + request.getWorkoutTemplateId()
                        )
                );

        verifyTemplateOwnership(template, user);

        schedule.setWorkoutTemplate(template);
        schedule.setScheduledDate(request.getScheduledDate());
        schedule.setScheduledTime(request.getScheduledTime());
        schedule.setNotes(request.getNotes());

        Schedule updatedSchedule =
                scheduleRepository.save(schedule);

        return toResponse(updatedSchedule);
    }

    public void deleteSchedule(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found: " + id
                        )
                );

        verifyOwnership(schedule, user);

        scheduleRepository.delete(schedule);
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void verifyOwnership(
            Schedule schedule,
            User user
    ) {

        if (!schedule.getUser().getId().equals(user.getId())) {

            throw new ResourceNotFoundException(
                    "Schedule not found: " + schedule.getId()
            );
        }
    }

    private void verifyTemplateOwnership(
            WorkoutTemplate template,
            User user
    ) {

        if (!template.getUser().getId().equals(user.getId())) {

            throw new ResourceNotFoundException(
                    "Workout template not found: " + template.getId()
            );
        }
    }

    private ScheduleResponse toResponse(
            Schedule schedule
    ) {

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getUser().getId(),
                schedule.getWorkoutTemplate().getId(),
                schedule.getWorkoutTemplate().getName(),
                schedule.getScheduledDate(),
                schedule.getScheduledTime(),
                schedule.getNotes(),
                schedule.getCreatedAt()
        );
    }
}