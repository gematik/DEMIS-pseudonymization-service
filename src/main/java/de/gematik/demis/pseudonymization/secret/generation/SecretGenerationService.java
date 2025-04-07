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

import de.gematik.demis.pseudonymization.config.SecretGenerationConfiguration;
import de.gematik.demis.pseudonymization.secret.model.AbstractSecretEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretRepository;
import java.util.Optional;
import org.slf4j.Logger;

/**
 * Generic Interface for the Secret Generation Service
 *
 * @param <T>
 */
abstract class SecretGenerationService<T extends AbstractSecretEntity> {
  private final SecretRepository<T> secretRepository;
  private final SecretGenerationConfiguration generationConfiguration;

  SecretGenerationService(
      final SecretRepository<T> secretRepository,
      final SecretGenerationConfiguration generationConfiguration) {
    this.secretRepository = secretRepository;
    this.generationConfiguration = generationConfiguration;
  }

  protected abstract Logger log();

  public abstract void createNewSecrets();

  boolean getInitOnMissing() {
    return this.generationConfiguration.initOnMissing();
  }

  private SecretGenerator secretGenerator() {
    return new SecretGenerator(
        this.generationConfiguration.supportedSymbols().toCharArray(),
        this.generationConfiguration.secretLength());
  }

  void createNewSecretEntityIfExpired() {
    if (isSecretExpired()) {
      this.secretRepository.lockTable();
      // check again secret validity with locked table
      if (isSecretExpired()) {
        createNewSecretEntity(computeNextId());
      }
    }
  }

  void createSecretIfNoneExists() {
    if (this.secretRepository.findLastUsedSecrets().isEmpty()) {
      this.secretRepository.lockTable();
      // Execute the function twice to generate "old" and "new" secret on an empty DB
      createNewSecretEntity(1);
      createNewSecretEntity(2);
    }
  }

  private void createNewSecretEntity(final int newId) {
    final var secretGenerator = secretGenerator();
    log().info("Creating new secret with ID {}", newId);
    this.secretRepository.addNewSecret(
        newId,
        secretGenerator.getRandomSecret(),
        secretGenerator.getRandomSecret(),
        secretGenerator.getRandomSecret(),
        secretGenerator.getRandomSecret());
  }

  private Optional<T> findValidSecretIfAny() {
    return Optional.ofNullable(
        this.secretRepository.findValidSecret(this.generationConfiguration.daysOfValidity()));
  }

  /**
   * Checks that no valid secrets (with a validity date within the configured range) exists.
   *
   * @return true if no valid secret has been found.
   */
  private boolean isSecretExpired() {
    return findValidSecretIfAny().isEmpty();
  }

  /**
   * Defines the next ID to be used for the INSERT operation in the Database.
   *
   * @return a new {@link AbstractSecretEntity} ID, otherwise by default 1 if the database is empty
   */
  private int computeNextId() {
    final var lastSecret = Optional.ofNullable(this.secretRepository.findLastCreatedSecret());
    return lastSecret.map(secretEntity -> secretEntity.getId() + 1).orElse(1);
  }
}
