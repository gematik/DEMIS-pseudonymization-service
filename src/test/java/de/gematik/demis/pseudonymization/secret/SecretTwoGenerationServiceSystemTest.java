package de.gematik.demis.pseudonymization.secret;

/*-
 * #%L
 * pseudonymization-service
 * %%
 * Copyright (C) 2025 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #L%
 */

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.gematik.demis.pseudonymization.secret.generation.SecretTwoGenerationService;
import de.gematik.demis.pseudonymization.secret.model.SecretTwoRepository;
import de.gematik.demis.pseudonymization.util.DatabaseConnector;
import de.gematik.demis.pseudonymization.util.SpringTestContainerStarter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
class SecretTwoGenerationServiceSystemTest extends SpringTestContainerStarter {
  @Autowired private SecretTwoGenerationService secretGenerationService;
  @Autowired private SecretTwoRepository secretRepository;
  @Autowired private JdbcTemplate jdbcTemplate;
  private DatabaseConnector databaseConnector;

  @NotNull
  private static ListAppender<ILoggingEvent> registerLogAppenderForService() {
    final var logAppender = new ListAppender<ILoggingEvent>();
    final Logger logger = (Logger) LoggerFactory.getLogger(SecretTwoGenerationService.class);
    logger.addAppender(logAppender);
    logAppender.start();
    return logAppender;
  }

  @BeforeEach
  void beforeEach() {
    log.info("########################");
    log.info("Reset Database to initial state");
    this.databaseConnector = new DatabaseConnector(this.jdbcTemplate);
    this.databaseConnector.cleanSecretTwoEntries();
    this.databaseConnector.writeInitialSecretTwoEntries();
  }

  @AfterEach
  void afterEach() {
    log.info("########################");
  }

  @Test
  void expectRotationOfSecretWorks() {
    final var initialSecrets =
        Assertions.assertDoesNotThrow(() -> this.secretRepository.findLastUsedSecrets());
    Assertions.assertDoesNotThrow(() -> this.secretGenerationService.createNewSecrets());
    final var rotatedSecrets =
        Assertions.assertDoesNotThrow(() -> this.secretRepository.findLastUsedSecrets());
    Assertions.assertNotEquals(initialSecrets, rotatedSecrets);
    Assertions.assertNotEquals(
        initialSecrets.getFirst().getId(), rotatedSecrets.getFirst().getId());
    Assertions.assertNotEquals(initialSecrets.getLast().getId(), rotatedSecrets.getLast().getId());
  }

  @Test
  void expectLockWorks() {
    // Create a custom Logger to catch log output
    final var logAppender = registerLogAppenderForService();
    // Perform execution in parallel
    IntStream.range(0, 4).parallel().forEach(this::performRotation);
    // Fire the DB notification only once
    final var notificationMessages =
        logAppender.list.stream()
            .map(ILoggingEvent::getMessage)
            .filter(message -> message.contains("Creating new secret with ID"))
            .toList();
    Assertions.assertEquals(1, notificationMessages.size());
  }

  @Test
  void expectRotationWorksAtGivenDayWhenExpired() {
    // Create a custom Logger to catch log output
    final var logAppender = registerLogAppenderForService();

    final var insertQueryTemplate =
        "INSERT INTO secrets_two(name_fcn_first,name_fcn_second,date_fcn_first,date_fcn_second, created_at) VALUES('%s', '%s', '%s', '%s', '%s');";
    Assertions.assertDoesNotThrow(
        () ->
            this.jdbcTemplate.execute(
                String.format(
                    insertQueryTemplate,
                    "AAAA",
                    "BBBB",
                    "CCCC",
                    "DDDD",
                    Timestamp.from(
                        LocalDate.now()
                            .minusDays(2190)
                            .atStartOfDay(ZoneId.of("UTC"))
                            .toInstant()))));
    Assertions.assertDoesNotThrow(
        () ->
            this.jdbcTemplate.execute(
                String.format(
                    insertQueryTemplate,
                    "AAAA",
                    "BBBB",
                    "CCCC",
                    "DDDD",
                    Timestamp.from(
                        LocalDate.now()
                            .minusDays(1095)
                            .atStartOfDay(ZoneId.of("UTC"))
                            .toInstant()))));

    this.secretGenerationService.createNewSecrets();

    // Fire the DB notification only once
    final var notificationMessages =
        logAppender.list.stream()
            .map(ILoggingEvent::getMessage)
            .filter(message -> message.contains("Creating new secret with ID"))
            .toList();
    Assertions.assertEquals(1, notificationMessages.size());
  }

  @Test
  void expectRotationDoesNotHappenWhenSecretIsNotExpired() {
    // Create a custom Logger to catch log output
    final var logAppender = registerLogAppenderForService();

    final var insertQueryTemplate =
        "INSERT INTO secrets_two(name_fcn_first,name_fcn_second,date_fcn_first,date_fcn_second, created_at) VALUES('%s', '%s', '%s', '%s', '%s');";
    Assertions.assertDoesNotThrow(
        () ->
            this.jdbcTemplate.execute(
                String.format(
                    insertQueryTemplate,
                    "AAAA",
                    "BBBB",
                    "CCCC",
                    "DDDD",
                    Timestamp.from(
                        LocalDate.now()
                            .minusDays(89)
                            .atStartOfDay(ZoneId.of("UTC"))
                            .toInstant()))));
    Assertions.assertDoesNotThrow(
        () ->
            this.jdbcTemplate.execute(
                String.format(
                    insertQueryTemplate,
                    "AAAA",
                    "BBBB",
                    "CCCC",
                    "DDDD",
                    Timestamp.from(
                        LocalDate.now()
                            .minusDays(44)
                            .atStartOfDay(ZoneId.of("UTC"))
                            .toInstant()))));

    this.secretGenerationService.createNewSecrets();

    // Fire the DB notification only once
    final var notificationMessages =
        logAppender.list.stream()
            .map(ILoggingEvent::getMessage)
            .filter(message -> message.contains("Creating new secret with ID"))
            .toList();
    Assertions.assertEquals(0, notificationMessages.size());
  }

  @Test
  void expectGenerationOfSecretsOnEmptyDatabaseWorks() {
    this.databaseConnector.cleanSecretTwoEntries();
    Assertions.assertDoesNotThrow(() -> this.secretGenerationService.onConstruct());
    final var result =
        Assertions.assertDoesNotThrow(() -> this.secretRepository.findLastUsedSecrets());
    Assertions.assertEquals(2, result.size());
    // Active
    Assertions.assertEquals(2, result.getFirst().getId());
    // Outdated
    Assertions.assertEquals(1, result.getLast().getId());
    result.forEach(
        secretEntity ->
            log.info(
                "{}: {}", secretEntity.getId(), secretEntity.getCreatedTimestamp().toString()));
  }

  private void performRotation(final int i) {
    log.info("Starting execution thread {}", i);
    this.secretGenerationService.createNewSecrets();
  }
}
