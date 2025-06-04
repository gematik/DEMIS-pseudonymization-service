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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ConsistentRequestValidator
    implements ConstraintValidator<ConsistentRequest, PseudonymizationRequest> {

  @Override
  public boolean isValid(
      final PseudonymizationRequest request,
      final ConstraintValidatorContext constraintValidatorContext) {
    // At least one of them has to be filled
    return isSet(removeSpecialCharacters(request.familyName()))
        || isSet(removeSpecialCharacters(request.firstName()))
        || isSet(removeSpecialCharacters((request.dateOfBirth())));
  }

  private String removeSpecialCharacters(final String input) {
    final StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  private boolean isSet(final String value) {
    return !Objects.requireNonNullElse(value, "").isBlank();
  }
}
