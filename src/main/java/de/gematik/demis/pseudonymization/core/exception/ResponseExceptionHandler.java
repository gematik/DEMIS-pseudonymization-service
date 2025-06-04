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

import de.gematik.demis.pseudonymization.core.PseudonymizationResponse;
import jakarta.validation.ValidationException;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Exception Handler for REST Operations. */
@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler {

  private static final String INTERNAL_ERROR_MESSAGE =
      "An Internal Server error has occurred. Please check the logs for more information about the issue.";

  /** Occurs when a validator encounters invalid data. Contains the affected field name */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<PseudonymizationResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String errorDetails =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));
    log.error("Invalid argument for request: {}", errorDetails);
    return ResponseEntity.badRequest().body(new PseudonymizationResponse(errorDetails));
  }

  protected String formatFieldError(FieldError fieldError) {
    if (fieldError.getField().equals("dateOfBirth")) {
      final String maskedValue = maskDate(Objects.toString(fieldError.getRejectedValue(), ""));
      return String.format(
          "Field '%s': '%s' is not a valid date format (expected one of: YYYY, mm.YYYY, dd.mm.YYYY) or out of range (older than 01.01.1800)",
          fieldError.getField(), maskedValue);
    } else {
      return String.format("Field '%s': Not a valid value)", fieldError.getField());
    }
  }

  /** Method is package-private to be exposed to tests. */
  @Nonnull
  static String maskDate(@Nonnull String input) {
    return input
        .chars()
        .mapToObj(
            c -> {
              if (Character.isDigit(c)) return "D";
              else if (Character.isLetter(c)) return "W";
              else if ("./- ".indexOf(c) >= 0) return String.valueOf((char) c);
              else return "?";
            })
        .collect(Collectors.joining());
  }

  /**
   * Triggers when something fails during validation. Does not necessarily have to be the validation
   * itself. Does not contain any details about the error.
   */
  @ExceptionHandler(ValidationException.class)
  protected ResponseEntity<PseudonymizationResponse> handleValidationException(
      ValidationException ex) {
    String errorDetails = ex.getMessage() != null ? ex.getMessage() : "Validation error occurred";
    log.error("Validation error occurred: {}", errorDetails);
    return ResponseEntity.badRequest().body(new PseudonymizationResponse(errorDetails));
  }

  @ExceptionHandler({IllegalArgumentException.class})
  protected ResponseEntity<PseudonymizationResponse> handleInvalidArgumentException(Exception ex) {
    log.info("Invalid argument exception: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(new PseudonymizationResponse("Invalid request"));
  }

  @ExceptionHandler(NullPointerException.class)
  protected ResponseEntity<PseudonymizationResponse> handleNullPointerException(
      NullPointerException ex) {
    log.warn("NullPointer exception", ex);
    return ResponseEntity.internalServerError()
        .body(new PseudonymizationResponse(INTERNAL_ERROR_MESSAGE));
  }
}
