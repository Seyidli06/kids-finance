package com.kidsfinance.simulation;

import com.kidsfinance.learning.LearningTopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SimulationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private LearningTopicRepository learningTopicRepository;

    @Autowired
    private SimulationScenarioRepository simulationScenarioRepository;

    @Autowired
    private SimulationOptionRepository simulationOptionRepository;

    @Test
    void shouldLoadSeededSimulationCatalog() {

        assertThat(
                learningTopicRepository.count()
        ).isEqualTo(4);

        assertThat(
                simulationScenarioRepository.count()
        ).isEqualTo(2);

        assertThat(
                simulationOptionRepository.count()
        ).isEqualTo(4);
    }
}