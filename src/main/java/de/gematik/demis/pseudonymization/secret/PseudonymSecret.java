package de.gematik.demis.pseudonymization.secret;

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

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import org.springframework.validation.annotation.Validated;

/**
 * Class containing the information about secrets used for pseudonymization.
 *
 * @param nameFirstFunction the secret for the first function for name (Fcn0)
 * @param nameSecondFunction the secret for the first function for name (Fcn1)
 * @param birthdateFirstFunction the secret for the first function for birthdate (Fcn0)
 * @param birthdateSecondFunction the secret for the first function for birthdate (Fcn1)
 */
@Validated
public record PseudonymSecret(
    @NotBlank String nameFirstFunction,
    @NotBlank String nameSecondFunction,
    @NotBlank String birthdateFirstFunction,
    @NotBlank String birthdateSecondFunction,
    @NotBlank LocalDateTime createdAt) {}
