package de.gematik.demis.pseudonymization.core.exception;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.FieldError;

class ResponseExceptionHandlerTest {

  private final ResponseExceptionHandler handler = new ResponseExceptionHandler();

  @Test
  void expectThatFormatFieldErrorReturnsFormattedErrorMessageWithDateOfBirth() {
    // Given
    FieldError fieldError = mock(FieldError.class);
    when(fieldError.getField()).thenReturn("dateOfBirth");
    when(fieldError.getRejectedValue()).thenReturn("15.13.2022");

    // When
    String result = handler.formatFieldError(fieldError);

    // Then
    assertThat(result).contains("Field 'dateOfBirth'");
    assertThat(result).contains("'DD.DD.DDDD'");
    assertThat(result).contains("expected one of: YYYY, mm.YYYY, dd.mm.YYYY");
  }

  @Test
  void expectThatFormatFieldErrorReturnsGenericErrorMessageWithOtherField() {
    // Given
    FieldError fieldError = mock(FieldError.class);
    when(fieldError.getField()).thenReturn("someOtherField");

    // When
    String result = handler.formatFieldError(fieldError);

    // Then
    assertThat(result).isEqualTo("Field 'someOtherField': Not a valid value)");
  }

  @Test
  void expectThatFormatFieldErrorHandlesNullProperly() {
    // Given
    FieldError fieldError = mock(FieldError.class);
    when(fieldError.getField()).thenReturn("dateOfBirth");
    when(fieldError.getRejectedValue()).thenReturn(null);

    // When
    String result = handler.formatFieldError(fieldError);

    // Then
    assertThat(result.contains("Field 'dateOfBirth'")).isTrue();
    assertThat(result.contains("''")).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "15.13.2022, DD.DD.DDDD",
    "Jan.2023, WWW.DDDD",
    "2022-01-15, DDDD-DD-DD",
    "15.01.2022!@#, DD.DD.DDDD???"
  })
  void expectThatMaskDateConvertsInputCorrectly(String input, String expected) {
    // When
    String result = ResponseExceptionHandler.maskDate(input);

    // Then
    assertThat(result).isEqualTo(expected);
  }
}
