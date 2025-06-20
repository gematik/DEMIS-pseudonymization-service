package de.gematik.demis.pseudonymization.core;

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

import de.gematik.demis.pseudonymization.secret.PseudonymSecret;
import de.gematik.demis.pseudonymization.secret.PseudonymSecretPair;
import de.gematik.demis.pseudonymization.secret.load.SecretOneLoaderService;
import de.gematik.demis.pseudonymization.secret.load.SecretTwoLoaderService;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Manages internal secret rotation and is a proxy to retrieve the correct secret based on a disease
 * code.
 */
@Service
public class PseudonymizationSecretManager {

  private final Set<String> diseasesWithSecretTwo;
  private final SecretOneLoaderService secretOneLoaderService;
  private final SecretTwoLoaderService secretTwoLoaderService;

  public PseudonymizationSecretManager(
      @Value("${ops.flag.secrets.two.diseases}") final Set<String> diseasesWithSecretTwo,
      final SecretOneLoaderService secretOneLoaderService,
      final SecretTwoLoaderService secretTwoLoaderService) {
    this.diseasesWithSecretTwo = diseasesWithSecretTwo;
    this.secretOneLoaderService = secretOneLoaderService;
    this.secretTwoLoaderService = secretTwoLoaderService;
  }

  /**
   * Returns the correct secret pair to use for the given diseaseCode. The secret pair is ready to
   * use and doesn't need to be salted with the disease code.
   */
  PseudonymSecretPair getSaltedSecretPair(final String diseaseCode) {
    final PseudonymSecretPair rawSecretPair;
    if (isSecretTwoRequired(diseaseCode)) {
      rawSecretPair = secretTwoLoaderService.currentSecretPair();
    } else {
      rawSecretPair = secretOneLoaderService.currentSecretPair();
    }

    /*
     * This is an ugly way of "cloning" the secret pair, but this way the caller
     * doesn't need to know about the salting process and can instead just use the secret.
     */
    return new PseudonymSecretPair(
        new PseudonymSecret(
            rawSecretPair.outdated().nameFirstFunction() + diseaseCode,
            rawSecretPair.outdated().nameSecondFunction() + diseaseCode,
            rawSecretPair.outdated().birthdateFirstFunction() + diseaseCode,
            rawSecretPair.outdated().birthdateSecondFunction() + diseaseCode,
            rawSecretPair.outdated().createdAt()),
        new PseudonymSecret(
            rawSecretPair.active().nameFirstFunction() + diseaseCode,
            rawSecretPair.active().nameSecondFunction() + diseaseCode,
            rawSecretPair.active().birthdateFirstFunction() + diseaseCode,
            rawSecretPair.active().birthdateSecondFunction() + diseaseCode,
            rawSecretPair.active().createdAt()));
  }

  @Scheduled(cron = "${secrets.one.reloading.cron-schedule}")
  @ConditionalOnProperty(prefix = "secrets.one.reloading", name = "enabled", havingValue = "true")
  public void reloadSecretOne() {
    this.secretOneLoaderService.reload();
  }

  /**
   * Creates a new set of secrets in case the current secrets are older than the allowed period of
   * time given.
   */
  @Scheduled(cron = "${secrets.two.reloading.cron-schedule}")
  @ConditionalOnProperty(prefix = "secrets.two.reloading", name = "enabled", havingValue = "true")
  public void reloadSecretTwo() {
    this.secretTwoLoaderService.reload();
  }

  /**
   * Check that the given disease is part of the list of diseases requiring the secret "two".
   *
   * @param disease the disease to check
   * @return true if the disease is part of the list, false otherwise
   */
  private boolean isSecretTwoRequired(final String disease) {
    if (Objects.isNull(disease) || disease.isBlank()) {
      return false;
    }

    return this.diseasesWithSecretTwo.stream().anyMatch(disease::startsWith);
  }
}
