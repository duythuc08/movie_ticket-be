package com.example.movie_ticket_be.cf.repository;

import com.example.movie_ticket_be.cf.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserActivityLogRepository
        extends JpaRepository<UserActivityLog, UserActivityLog.UserActivityLogId>,
                JpaSpecificationExecutor<UserActivityLog> {
}
