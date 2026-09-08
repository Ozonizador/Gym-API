package com.gym.gym_api.repository;

import com.gym.gym_api.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {
}