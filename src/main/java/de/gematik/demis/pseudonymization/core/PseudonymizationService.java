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
 * #L%
 */

import static de.gematik.demis.pseudonymization.config.BloomPseudonymConfiguration.BIRTHDAY_PSEUDONYM_SERVICE_FACTORY;
import static de.gematik.demis.pseudonymization.config.BloomPseudonymConfiguration.NAME_PSEUDONYM_SERVICE_FACTORY;

import de.gematik.demis.pseudonymization.config.PseudonymizationMetrics;
import de.gematik.demis.pseudonymization.secret.PseudonymSecret;
import de.gematik.demis.pseudonymization.secret.PseudonymSecretPair;
import de.gematik.demis.pseudonymization.shared.BloomBasedPseudonymService;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * This service calculates a pseudonym, including §7.3 Notification Events.
 *
 * <p>This class is the heard of the pseudonymization-service. It calculates pseudonyms to a given
 * surname, firstname and date of birth. The calculation is done with a current secret and the
 * secret that was used (outdated secret) before. If the pseudonym was already calculated with the
 * same input data and secret it will be returned from a cache.
 *
 * <p>The service provides also a method to change the current secret und the old current secret is
 * change to the outdated secret.
 */
@Validated
@Service
@Slf4j
public class PseudonymizationService {

  private final PseudonymizationSecretManager secretManager;

  private final BloomBasedPseudonymServiceFactory namePseudonymServiceFactory;
  private final BloomBasedPseudonymServiceFactory birthdayPseudonymServiceFactory;
  private final PseudonymizationMetrics metrics;

  public PseudonymizationService(
      final PseudonymizationSecretManager secretManager,
      @Qualifier(NAME_PSEUDONYM_SERVICE_FACTORY)
          final BloomBasedPseudonymServiceFactory namePseudonymServiceFactory,
      @Qualifier(BIRTHDAY_PSEUDONYM_SERVICE_FACTORY)
          final BloomBasedPseudonymServiceFactory birthdayPseudonymServiceFactory,
      final PseudonymizationMetrics actuatorEndpoint) {
    this.secretManager = secretManager;
    this.namePseudonymServiceFactory = namePseudonymServiceFactory;
    this.birthdayPseudonymServiceFactory = birthdayPseudonymServiceFactory;
    this.metrics = actuatorEndpoint;
  }

  private Pseudonym calculate(final PseudonymSecret secret, final PseudonymizationRequest request) {
    final BloomBasedPseudonymService namePseudonymService =
        namePseudonymServiceFactory.create(secret.nameFirstFunction(), secret.nameSecondFunction());
    final BloomBasedPseudonymService birthdayPseudonymService =
        birthdayPseudonymServiceFactory.create(
            secret.birthdateFirstFunction(), secret.birthdateSecondFunction());

    final List<String> familyNamePseudonym = namePseudonymService.process(request.familyName());
    final List<String> firstNamePseudonym = namePseudonymService.process(request.firstName());
    // We specifically test for isEmpty() here, because "  " might be the intention, and we can work
    // with that.
    final String birthdayPseudonym;
    if (request.dateOfBirth().isBlank()) {
      birthdayPseudonym = "";
    } else {
      birthdayPseudonym = birthdayPseudonymService.processRaw(request.dateOfBirth());
    }

    return new Pseudonym(
        familyNamePseudonym, firstNamePseudonym, birthdayPseudonym, request.diseaseCode());
  }

  /**
   * Generates pseudonym to the given input and the current and outdated secret, based on disease
   * type.
   *
   * @param request input data to calculate pseudonym
   * @return pseudonym
   */
  @Observed(
      name = "generate-pseudonyms",
      contextualName = "pseudonyms",
      lowCardinalityKeyValues = {"pseudonyms", "fhir"})
  PseudonymizationResponse generatePseudonym(@Valid final PseudonymizationRequest request) {
    final String diseaseCode = request.diseaseCode();
    final PseudonymSecretPair diseaseBasedSecret = secretManager.getSaltedSecretPair(diseaseCode);
    final Pseudonym active = calculate(diseaseBasedSecret.active(), request);
    final Pseudonym outdated = calculate(diseaseBasedSecret.outdated(), request);

    metrics.incrementTotal();

    return new PseudonymizationResponse(outdated, active);
  }
}
