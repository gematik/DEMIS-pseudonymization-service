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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

class ConsistentRequestValidatorTest {

  // Currently not used, just referenced for readability
  private final ConstraintValidatorContext ctx = null;
  private final ConsistentRequestValidator validator = new ConsistentRequestValidator();

  @Test
  void thatDateOfBirthValidationWorks() {
    var req = new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "08.2023");
    assertThat(validator.isValid(req, ctx)).isTrue();
    req = new PseudonymizationRequest("isignored", "cvd", "Power", "Max", "2023");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatFamilyNameCanBeOptional() {
    var req = new PseudonymizationRequest("isSet", "isSet", "", "isSet", "isSet");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatFirstNameCanBeOptional() {
    var req = new PseudonymizationRequest("isSet", "isSet", "isSet", "", "isSet");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatDateOfBirthCanBeOptional() {
    var req = new PseudonymizationRequest("isSet", "isSet", "isSet", "isSet", "");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatTwoOutOfThreeParametersCanBeEmpty() {
    var req = new PseudonymizationRequest("isSet", "isSet", "isSet", "", "");
    assertThat(validator.isValid(req, ctx)).isTrue();
    req = new PseudonymizationRequest("isSet", "isSet", "", "isSet", "");
    assertThat(validator.isValid(req, ctx)).isTrue();
    req = new PseudonymizationRequest("isSet", "isSet", "", "", "isSet");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatNullIsHandledGracefully() {
    var req = new PseudonymizationRequest("isSet", "isSet", null, "isSet", "isSet");
    assertThat(validator.isValid(req, ctx)).isTrue();
    req = new PseudonymizationRequest("isSet", "isSet", "isSet", null, "isSet");
    assertThat(validator.isValid(req, ctx)).isTrue();
    req = new PseudonymizationRequest("isSet", "isSet", "isSet", "isSet", null);
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatAllParametersBeingEmptyIsInvalid() {
    var req = new PseudonymizationRequest("isSet", "isSet", "", "", "");
    assertThat(validator.isValid(req, ctx)).isFalse();
  }

  @Test
  void thatSpecialCharactersAreConsideredAbsent() {
    var req = new PseudonymizationRequest("isSet", "isSet", "!@#$%^&*()=+", "", "");
    assertThat(validator.isValid(req, ctx)).isFalse();

    req = new PseudonymizationRequest("isSet", "isSet", "Typo!@#$%^&*()=+", "", "");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }

  @Test
  void thatOnlyNumbersAreConsideredValid() {
    var req = new PseudonymizationRequest("isSet", "isSet", "1234", "1234", "1234");
    assertThat(validator.isValid(req, ctx)).isTrue();
  }
}
