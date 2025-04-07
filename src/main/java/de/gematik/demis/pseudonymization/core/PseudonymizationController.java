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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Defines the API REST Endpoints for the application. */
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class PseudonymizationController {

  private static final String LEGACY_MEDIA_TYPE = "application/vnd.demis_pseudonymization+json";
  private final PseudonymizationService pseudonymizationService;

  @PostMapping(
      path = Endpoint.PSEUDONYMIZATION,
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public PseudonymizationResponse generatePseudonym(
      @RequestBody @Valid PseudonymizationRequest content) {
    log.info("Generate Pseudonym");
    return pseudonymizationService.generatePseudonym(content);
  }

  @PostMapping(
      path = Endpoint.PSEUDONYMIZATION,
      consumes = {LEGACY_MEDIA_TYPE},
      produces = {LEGACY_MEDIA_TYPE})
  @ResponseStatus(HttpStatus.OK)
  public PseudonymizationResponse generatePseudonymWithLegacyMediaTypes(
      @RequestBody @Valid PseudonymizationRequest content) {
    log.info("Generate Pseudonym with legacy MediaType {}", LEGACY_MEDIA_TYPE);
    return pseudonymizationService.generatePseudonym(content);
  }
}
