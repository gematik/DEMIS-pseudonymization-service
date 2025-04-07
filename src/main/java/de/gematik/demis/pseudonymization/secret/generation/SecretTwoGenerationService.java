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
 * #L%
 */

import de.gematik.demis.pseudonymization.config.SecretTwoGenerationConfiguration;
import de.gematik.demis.pseudonymization.secret.model.SecretTwoEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretTwoRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service responsible for executing the rotation of {@link SecretTwoEntity} at a given Cron
 * Schedule.
 */
@ConditionalOnProperty(prefix = "secrets.two.generation", name = "enabled", havingValue = "true")
@Validated
@Service
@Slf4j
public class SecretTwoGenerationService extends SecretGenerationService<SecretTwoEntity> {

  /**
   * Constructs an instance of {@link SecretTwoGenerationService}.
   *
   * @param extendedSecretRepository the JDBC Repository for standard secrets
   * @param secretGenerationConfiguration the configuration for generating secrets
   */
  public SecretTwoGenerationService(
      final SecretTwoRepository extendedSecretRepository,
      final SecretTwoGenerationConfiguration secretGenerationConfiguration) {
    super(extendedSecretRepository, secretGenerationConfiguration);
  }

  /**
   * When constructed, the object checks for an empty Table and if there are no secrets, then
   * creates an "old" and a "new" secret.
   */
  @PostConstruct
  @Transactional
  public void onConstruct() {
    if (getInitOnMissing()) {
      log.info("Checking if Database contains valid extended secrets");
      createSecretIfNoneExists();
    }
  }

  /**
   * Creates a new set of secrets in case the current secrets are older than the allowed period of
   * time given.
   */
  @Scheduled(cron = "${secrets.two.generation.cron-schedule}")
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
