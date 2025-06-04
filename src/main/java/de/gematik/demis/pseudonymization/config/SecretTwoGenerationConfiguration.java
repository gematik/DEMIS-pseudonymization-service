package de.gematik.demis.pseudonymization.config;

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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Defines the configuration for the generation of secrets "two" (Secrets used for §7.3 Pseudonyms).
 *
 * @param enabled activates the generation of secrets
 * @param initOnMissing initializes the database with 2 new secrets if empty
 * @param daysOfValidity the maximum number of days for which the active secret is valid
 * @param secretLength the length of the generated secret
 * @param supportedSymbols the list of symbols to be used for generating the secrets
 * @param cronSchedule the Cron Task Schedule used for performing the generation of new secrets, if
 *     enabled
 */
@Validated
@ConfigurationProperties(prefix = "secrets.two.generation")
public record SecretTwoGenerationConfiguration(
    boolean enabled,
    boolean initOnMissing,
    @Min(1) @Max(1200) int daysOfValidity,
    @Min(1) @Max(70) int secretLength,
    @NotBlank String supportedSymbols,
    @NotBlank String cronSchedule)
    implements SecretGenerationConfiguration {}
