package com.kidsfinance.learning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningTopicRepository
        extends JpaRepository<LearningTopic, Long> {

    Optional<LearningTopic> findByCode(String code);
}