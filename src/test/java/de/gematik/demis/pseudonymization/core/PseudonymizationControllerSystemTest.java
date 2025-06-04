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

import de.gematik.demis.pseudonymization.util.DataGenerator;
import de.gematik.demis.pseudonymization.util.DatabaseConnector;
import de.gematik.demis.pseudonymization.util.SpringTestContainerStarter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
class PseudonymizationControllerSystemTest extends SpringTestContainerStarter {

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void beforeEach() {
    DatabaseConnector databaseConnector = new DatabaseConnector(jdbcTemplate);
    databaseConnector.cleanSecretOneEntries();
    databaseConnector.writeInitialSecretOneEntries();
  }

  @Test
  void expectError415OnBadContentTypeWhenGeneratingPseudonym() {
    Assertions.assertThrows(
        HttpClientErrorException.UnsupportedMediaType.class,
        () ->
            sendGeneratePseudonymRequestWithWrongContentTypeAndAccept(
                MediaType.APPLICATION_PDF_VALUE));
  }

  @Test
  void expectError406OnBadContentTypeWhenGeneratingPseudonym() {
    Assertions.assertThrows(
        HttpClientErrorException.NotAcceptable.class,
        () ->
            sendGeneratePseudonymRequestWithWrongContentTypeAndAccept(
                DataGenerator.LEGACY_CONTENT_TYPE));
  }

  @Test
  void expectError400OnMissingDiseaseCodeWhenGeneratingPseudonym() {
    PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "", "Power", "Max", "07.08.2023");
    Assertions.assertThrows(
        HttpClientErrorException.BadRequest.class,
        () ->
            sendGeneratePseudonymRequest(
                new ObjectMapper().writeValueAsString(pseudonymizationRequest)));
  }

  @Test
  void expectError400OnBadRequestWhenGeneratingPseudonym() {
    PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("", "", null, null, "07.08.2023");
    Assertions.assertThrows(
        HttpClientErrorException.BadRequest.class,
        () ->
            sendGeneratePseudonymRequest(
                new ObjectMapper().writeValueAsString(pseudonymizationRequest)));
  }

  @Test
  void expect200WhenGeneratingPseudonymWithNameContainingQuestionMark() {
    final var pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "cvd", "Power?", "Max?", "07.08.2023");
    Assertions.assertDoesNotThrow(
        () ->
            sendGeneratePseudonymRequest(
                new ObjectMapper().writeValueAsString(pseudonymizationRequest)));
  }

  @Test
  void expect200WhenGeneratingPseudonym() {
    final var pseudonymizationRequest =
        new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "07.08.2023");
    final var response =
        Assertions.assertDoesNotThrow(
            () ->
                sendGeneratePseudonymRequest(
                    new ObjectMapper().writeValueAsString(pseudonymizationRequest)));
    Assertions.assertEquals(200, response.getStatusCode().value());
  }

  @Test
  void expectError400OnBadRequestForInconsistentPersonData() {
    PseudonymizationRequest pseudonymizationRequest =
        new PseudonymizationRequest("isSet", "isSet", null, "", "");
    Assertions.assertThrows(
        HttpClientErrorException.BadRequest.class,
        () ->
            sendGeneratePseudonymRequest(
                new ObjectMapper().writeValueAsString(pseudonymizationRequest)));
  }

  @Test
  void expectError200ForPatchyPersonData() {
    final ObjectMapper objectMapper = new ObjectMapper();
    ResponseEntity<PseudonymizationResponse> response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest pseudonymizationRequest =
                  new PseudonymizationRequest("Anything", "Anything", "", "isSet", "2022");
              return sendGeneratePseudonymRequest(
                  objectMapper.writeValueAsString(pseudonymizationRequest));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());

    response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest pseudonymizationRequest =
                  new PseudonymizationRequest("Anything", "Anything", "isSet", "", "2022");
              return sendGeneratePseudonymRequest(
                  objectMapper.writeValueAsString(pseudonymizationRequest));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());

    response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest pseudonymizationRequest =
                  new PseudonymizationRequest("Anything", "Anything", "isSet", "isSet", "");
              return sendGeneratePseudonymRequest(
                  objectMapper.writeValueAsString(pseudonymizationRequest));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());
  }

  @Test
  void acceptsARangeOfDateValues() {
    final ObjectMapper objectMapper = new ObjectMapper();
    ResponseEntity<PseudonymizationResponse> response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest request =
                  new PseudonymizationRequest("isSet", "isSet", null, "", "2022");
              return sendGeneratePseudonymRequest(objectMapper.writeValueAsString(request));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());

    response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest request =
                  new PseudonymizationRequest("isSet", "isSet", null, "", "10.2022");
              return sendGeneratePseudonymRequest(objectMapper.writeValueAsString(request));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());

    response =
        Assertions.assertDoesNotThrow(
            () -> {
              final PseudonymizationRequest request =
                  new PseudonymizationRequest("isSet", "isSet", null, "", "01.10.2022");
              return sendGeneratePseudonymRequest(objectMapper.writeValueAsString(request));
            });
    Assertions.assertEquals(200, response.getStatusCode().value());
  }
}
