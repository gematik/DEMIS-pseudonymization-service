package de.gematik.demis.pseudonymization.secret.load;

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

import de.gematik.demis.pseudonymization.secret.EntityConverter;
import de.gematik.demis.pseudonymization.secret.PseudonymSecretPair;
import de.gematik.demis.pseudonymization.secret.model.AbstractSecretEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretRepository;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;

/**
 * Service responsible for loading an Entity from the database.
 *
 * @param <T> - the type of Secret Entity, Standard or Extended (§7.3)
 */
public abstract class SecretLoaderService<T extends AbstractSecretEntity> {

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final SecretRepository<T> secretRepository;
  private PseudonymSecretPair currentSecretPair;

  protected SecretLoaderService(final SecretRepository<T> secretRepository) {
    this.secretRepository = secretRepository;
  }

  protected abstract Logger log();

  /**
   * Returns the current (cached) standard secret pair.
   *
   * @return the cached instance of {@link PseudonymSecretPair}
   */
  public PseudonymSecretPair currentSecretPair() {
    if (Objects.isNull(this.currentSecretPair)) {
      reload();
    }
    return this.currentSecretPair;
  }

  /**
   * Imports the Standard Secret Pair from the database and returns it. Internally it updates the
   * cached secrets.
   *
   * @return an instance of {@link PseudonymSecretPair}
   */
  public PseudonymSecretPair secretPairFromDatabase() {
    log().info("Loading Standard Secrets from Database");
    this.currentSecretPair = importLastSecretsFromDatabase();
    return this.currentSecretPair;
  }

  public void reload() {
    this.currentSecretPair = importLastSecretsFromDatabase();
  }

  /**
   * Returns the current (cached) standard secret pair.
   *
   * @return the cached instance of {@link PseudonymSecretPair}
   */
  protected PseudonymSecretPair importLastSecretsFromDatabase() {
    try {
      lock.writeLock().lock();
      final var lastSecrets = secretRepository.findLastUsedSecrets();
      if (lastSecrets.size() != 2) {
        throw new IllegalStateException("Secrets could not be found in the database");
      }

      final var activeSecret = EntityConverter.fromDatabaseEntity(lastSecrets.get(0));
      final var outdatedSecret = EntityConverter.fromDatabaseEntity(lastSecrets.get(1));
      log().debug("Configuration imported from {}", secretRepository.getClass().getSimpleName());
      return new PseudonymSecretPair(outdatedSecret, activeSecret);
    } finally {
      lock.writeLock().unlock();
    }
  }
}
