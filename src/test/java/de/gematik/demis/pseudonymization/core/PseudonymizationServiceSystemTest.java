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

import de.gematik.demis.pseudonymization.secret.generation.SecretOneGenerationService;
import de.gematik.demis.pseudonymization.secret.generation.SecretTwoGenerationService;
import de.gematik.demis.pseudonymization.secret.load.SecretOneLoaderService;
import de.gematik.demis.pseudonymization.secret.load.SecretTwoLoaderService;
import de.gematik.demis.pseudonymization.util.DatabaseConnector;
import de.gematik.demis.pseudonymization.util.SpringTestContainerStarter;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
class PseudonymizationServiceSystemTest extends SpringTestContainerStarter {

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PseudonymizationSecretManager secretManager;
  @Autowired private PseudonymizationService pseudonymizationService;
  @Autowired private SecretOneLoaderService secretOneLoaderSvc;
  @Autowired private SecretTwoLoaderService secretTwoLoaderSvc;
  @Autowired private SecretOneGenerationService secretOneRotationService;
  @Autowired private SecretTwoGenerationService secretTwoRotationService;

  @BeforeEach
  void beforeEach() {
    final DatabaseConnector databaseConnector = new DatabaseConnector(jdbcTemplate);
    databaseConnector.cleanSecretOneEntries();
    databaseConnector.cleanSecretTwoEntries();
    databaseConnector.writeInitialSecretOneEntries();
    databaseConnector.writeInitialSecretTwoEntries();
    secretManager.rotateSecretOne();
    secretManager.rotateSecretTwo();
  }

  @Test
  void generatePseudonymTwiceAndCheckResultsAreTheSame() {
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("someId", "covid19", "Musterfrau", "Maria", "01.01.1990");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    Assertions.assertEquals(firstResponse, secondResponse);
  }

  @Test
  void expectPseudonymsWithDifferentDiseaseCodesAndFromSameSecretAreDifferent() {
    // WHEN Pseudonym for CVD (diseaseCode from first secret) has been computed
    final PseudonymizationRequest pseudonymizationRequestCvd =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestCvd);

    // AND Pseudonym for HEV (also diseaseCode from first secret) has been computed
    final PseudonymizationRequest pseudonymizationRequestHev =
        new PseudonymizationRequest("isignored", "hev", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestHev);

    // THEN both pseudonyms are different
    Assertions.assertNotEquals(firstResponse, secondResponse);
  }

  @Test
  void expectPseudonymsWithDifferentDiseaseCodesAndFromDifferentSecretsAreDifferent() {
    // WHEN Pseudonym for CVD (diseaseCode from first secret) has been computed
    final PseudonymizationRequest pseudonymizationRequestCvd =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestCvd);

    // AND Pseudonym for HIV (diseaseCode from second secret) has been computed
    final PseudonymizationRequest pseudonymizationRequestHev =
        new PseudonymizationRequest("isignored", "hiv", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestHev);

    // THEN both pseudonyms are different
    Assertions.assertNotEquals(firstResponse, secondResponse);
  }

  @Test
  void expectPseudonymsWithPlatonInGreekLettersAndPlatonInLatinLettersAreEqual() {
    // WHEN Pseudonym for Platon in Greek letters has been computed
    final PseudonymizationRequest pseudonymizationRequestCvd =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Πλάτων", "13.08.2023");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestCvd);

    // AND Pseudonym for Platon in Latin letters has been computed
    final PseudonymizationRequest pseudonymizationRequestHev =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Platon", "13.08.2023");
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequestHev);

    // THEN both pseudonyms are equal
    Assertions.assertEquals(firstResponse, secondResponse);
  }

  /**
   * This test is to ensure that this (new) service generates the same pseudonyms as the old
   * replaced service (espri-demis-pseudonymization).
   */
  @Test
  void expectPseudonymWithAllParametersWork() {
    final PseudonymizationResponse expectedResponse =
        new PseudonymizationResponse(
            new Pseudonym(
                List.of(
                    "AAgAAAoAAAAAAAAACFAAAgAAAAAAEAIgAwAAAAAIBAAoCAggAAABAAIgAiAABAABCAASAAgBCAAACAIAAgAABAAAACAAAggAIAIABAAABggAgIgAAJIAgAAAAAAAAAAAAoAAAAEAMAAICAAIAQAAAAAAAgAAAAAAAACCAEBAgAA="),
                List.of(
                    "MAAABCCAAAAQQICDAEAAAAgAAAEAAAAgAIAAQQAAAAAAAAABAAAAgAAAAAEAAAAAAACAAQCAAAEAAAABAAAAQAAAAIEAAAAAAAEBAQAAAgAAAIQBAAAAAAAAABAAAAAAgAAAAEAEAAAACAAAAACBAAAAAAAAAAAEAAAACIAAAQA="),
                "AgABFBCAQ2KiGIgIAiBKQAGgmACiBBAxdABBhQAigAAKIwIuCQAFBWQDIJMCiCcEgAAgECBRgAGhMDBbBQAAYQ==",
                "cvd"),
            new Pseudonym(
                List.of(
                    "AAIAAIAAQCAAAAAQAABAAQACAAAAACAALAAgQAgIAAACAFAEAAAAAIAAlAAIAAAAIAQAAYAADBAAEAACAgAAAAIAAASAAEAAAAECiAAABAAAAAAQAAAAACACwCAAAAAAAAgAQQAAGEQAAAAIQAgAAAACAQAAAAABACBAAQkAAAA="),
                List.of(
                    "AAABCAAABgAAAAAAAEkAACAAAAAAAAAAgABEAAAAABEAEgAAAIACAAAQABBAAAgAAAAAEAAgAEAAAAAAAADAAAAAGAAAQAAAAEQAAAgAAAAQAAQBQAAAIAAAAAQggAAAAAAAAAAAAAAABAAAABAAABEAIQACAAAAAAQIAAAAAIA="),
                "PIApBMgQAAgNgTAAAIxDCIABAAAZyEUGBcQAUAIKQEIDwAOAELICQTAAAD8QgIEQwKAUCUAUNMQBABAgEIUiAA==",
                "cvd"));

    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse actualResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    Assertions.assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void expectRotationOfSecretOneProducesNewPseudonyms() {
    // WHEN An initial generated Pseudonym has been computed
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    // GIVEN the trigger of a Rotation of Secrets
    Assertions.assertDoesNotThrow(() -> secretOneRotationService.createNewSecrets());
    // AND Reload secrets
    Assertions.assertDoesNotThrow(() -> secretManager.rotateSecretOne());
    // THEN generate a new Pseudonym
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    // AND Outdated Pseudonym Is now The Old ActiveOne
    Assertions.assertEquals(firstResponse.activePseudonym(), secondResponse.outdatedPseudonym());
    // AND Active Pseudonym Is Different
    Assertions.assertNotEquals(firstResponse.activePseudonym(), secondResponse.activePseudonym());
  }

  @Test
  void expectRotationOfSecretTwoProducesNewPseudonyms() {
    // WHEN An initial generated Pseudonym has been computed
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "hiv", "Power", "Max", "13.08.2023");
    final PseudonymizationResponse firstResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    // GIVEN the trigger of a Rotation of Secrets
    Assertions.assertDoesNotThrow(() -> secretTwoRotationService.createNewSecrets());
    // AND Reload secrets
    Assertions.assertDoesNotThrow(() -> secretManager.rotateSecretTwo());
    // THEN generate a new Pseudonym
    final PseudonymizationResponse secondResponse =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);
    // AND Outdated Pseudonym Is now The Old ActiveOne
    Assertions.assertEquals(firstResponse.activePseudonym(), secondResponse.outdatedPseudonym());
    // AND Active Pseudonym Is Different
    Assertions.assertNotEquals(firstResponse.activePseudonym(), secondResponse.activePseudonym());
  }

  @Test
  void expectReloadOfSecretOneDoesNothingWhenNoRotationHappens() {

    final var initialSecrets = secretOneLoaderSvc.secretPairFromDatabase();
    // Reload
    Assertions.assertDoesNotThrow(() -> secretManager.rotateSecretOne());

    final var newSecrets = secretOneLoaderSvc.secretPairFromDatabase();

    Assertions.assertEquals(initialSecrets, newSecrets);
  }

  @Test
  void expectReloadOfSecretTwoDoesNothingWhenNoRotationHappens() {

    final var initialSecrets = secretTwoLoaderSvc.secretPairFromDatabase();
    // Reload
    Assertions.assertDoesNotThrow(() -> secretManager.rotateSecretTwo());

    final var newSecrets = secretTwoLoaderSvc.secretPairFromDatabase();

    Assertions.assertEquals(initialSecrets, newSecrets);
  }

  @Test
  void expectThatEmptyDateOfBirthInRequestProducesEmptyElement() {
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("test", "nvp", "", "Max", "");

    final PseudonymizationResponse response =
        pseudonymizationService.generatePseudonym(pseudonymizationRequest);

    Assertions.assertTrue(response.activePseudonym().dateOfBirth().isEmpty());
    Assertions.assertTrue(response.outdatedPseudonym().dateOfBirth().isEmpty());
  }

  @Test
  void expectThatBlankDateOfBirthInRequestProducesElement() {
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("test", "nvp", "", "Max", "     ");

    Assertions.assertThrows(
        ConstraintViolationException.class,
        () -> pseudonymizationService.generatePseudonym(pseudonymizationRequest));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2023/08/13",
        "13.08.2023 12:00:00",
        "2023.13.08",
        "13.08.23",
        "JA.2023",
        "1.1.2015",
        "1.12.2015",
        "01.1.2015"
      })
  void expectValidationFailsForInvalidDateFormat(String invalidDate) {
    // GIVEN an invalid date format
    final PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("test", "nvp", "", "Max", invalidDate);

    // THEN expect a ConstraintViolationException
    Assertions.assertThrows(
        ConstraintViolationException.class,
        () -> pseudonymizationService.generatePseudonym(pseudonymizationRequest));
  }
}
