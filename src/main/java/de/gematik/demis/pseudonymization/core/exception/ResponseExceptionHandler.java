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
 * #L%
 */

import de.gematik.demis.pseudonymization.core.PseudonymizationResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Exception Handler for REST Operations. */
@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler {

  private static final String INTERNAL_ERROR_MESSAGE =
      "An Internal Server error has occurred. Please check the logs for more information about the issue.";

  @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
  protected ResponseEntity<PseudonymizationResponse> handleConstraint(Exception ex) {
    // Do not log the full message, it can contain sensitive data
    log.error("Invalid argument for request");
    return ResponseEntity.badRequest().body(new PseudonymizationResponse("Invalid request"));
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
