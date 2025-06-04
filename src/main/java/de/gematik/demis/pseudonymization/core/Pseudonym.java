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
import java.util.List;
import java.util.Objects;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record Pseudonym(
    List<String> familyName, List<String> firstName, String dateOfBirth, String diseaseCode) {
  public Pseudonym {
    /*
    We need to be able to generate pseudonyms, even if parts of the request are empty (e.g. no family name was given).
    The legacy implementation will return `null` values for missing parts. `null` usually leads to NullPointerExceptions.
    To avoid this, we convert possible null values into their empty equivalent. The new implementation will return empty
    values for empty input.
     */
    familyName = Objects.requireNonNullElse(familyName, List.of());
    firstName = Objects.requireNonNullElse(firstName, List.of());
    dateOfBirth = Objects.requireNonNullElse(dateOfBirth, "");
  }
}
