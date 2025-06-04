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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/** Pseudonymization Request structure. */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@ConsistentRequest
public record PseudonymizationRequest(
    String notificationBundleId,
    @NotBlank(message = "Property type is mandatory")
        @Pattern(
            regexp = "demisPseudonymi[sz]ationRequest",
            message = "type has not the value demisPseudonymizationRequest")
        String type,
    @NotBlank(message = "Property diseaseCode is mandatory") String diseaseCode,
    String familyName,
    String firstName,
    // Optional parameter, if set then match one of YYYY, mm.YYYY, dd.mm.YYYY
    @Pattern(
            regexp =
                "(^(18|19|20)\\d\\d$|(0[1-9]|1[012]).(18|19|20)\\d\\d$|(0[1-9]|[12][0-9]|3[01]).(0[1-9]|1[012]).(18|19|20)\\d\\d$)?")
        String dateOfBirth) {

  public static final String DEFAULT_TYPE = "demisPseudonymizationRequest";

  public PseudonymizationRequest(
      final String notificationBundleId,
      final String diseaseCode,
      final String familyName,
      final String firstName,
      final String dateOfBirth) {
    this(
        notificationBundleId,
        DEFAULT_TYPE,
        diseaseCode,
        Objects.requireNonNullElse(familyName, ""),
        Objects.requireNonNullElse(firstName, ""),
        Objects.requireNonNullElse(dateOfBirth, ""));
  }
}
