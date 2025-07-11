package de.gematik.demis.pseudonymization.secret.generation;

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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import de.gematik.demis.pseudonymization.config.SecretOneGenerationConfiguration;
import de.gematik.demis.pseudonymization.secret.model.SecretOneEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretOneRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service responsible for executing the rotation of {@link SecretOneEntity} at a given Cron
 * Schedule.
 */
@ConditionalOnProperty(prefix = "secrets.one.generation", name = "enabled", havingValue = "true")
@Validated
@Service
@Slf4j
public class SecretOneGenerationService extends SecretGenerationService<SecretOneEntity> {

  /**
   * Constructs an instance of {@link SecretOneGenerationService}.
   *
   * @param standardSecretRepository the JDBC Repository for standard secrets
   * @param secretGenerationConfiguration the configuration for generating secrets
   */
  public SecretOneGenerationService(
      final SecretOneRepository standardSecretRepository,
      final SecretOneGenerationConfiguration secretGenerationConfiguration) {

    super(standardSecretRepository, secretGenerationConfiguration);
  }

  /**
   * When constructed, the object checks for an empty Table and if there are no secrets, then
   * creates an "old" and a "new" secret.
   */
  @PostConstruct
  @Transactional
  public void onConstruct() {
    if (getInitOnMissing()) {
      log.info("Checking if Database contains valid standard secrets");
      createSecretIfNoneExists();
    }
  }

  /**
   * Creates a new set of secrets in case the current secrets are older than the allowed period of
   * time given.
   */
  @Scheduled(cron = "${secrets.one.generation.cron-schedule}")
  @Transactional
  @Override
  public void createNewSecrets() {
    createNewSecretEntityIfExpired();
  }

  @Override
  public Logger log() {
    return log;
  }
}
