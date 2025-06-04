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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * This is the Configuration identifying the properties used to create a Pseudonym.
 *
 * @param driver the Driver Class Name of Filter to be used, typically
 *     "de.fraunhofer.fokus.adep.pseudonymization.Bloom"
 * @param name the Object containing parameters for the generation of Pseudonym for Name
 * @param birthdate the Object containing parameters for the generation of Pseudonym for Birthdate
 */
@Validated
@ConfigurationProperties(prefix = "pseudonym.filter")
public record PseudonymFilterConfiguration(
    @NotBlank String driver, @NotNull Name name, @NotNull Birthdate birthdate) {

  /**
   * Configuration for the generation of Pseudonym of Names
   *
   * @param length the size of the Filter
   * @param fcn0 the first function type, typically HmacSHA1
   * @param fcn1 the second function type, typically HmacMD5
   * @param iterations the number of iterations for the hash function
   * @param ngram the gram size
   */
  public record Name(
      @Max(value = 2048) int length,
      HashType fcn0,
      HashType fcn1,
      @Max(value = 50) int iterations,
      @Max(value = 50) int ngram) {}

  /**
   * Configuration for the generation of Pseudonym of Birthdates
   *
   * @param length the size of the Filter
   * @param fcn0 the first function type, typically HmacSHA1
   * @param fcn1 the second function type, typically HmacMD5
   * @param iterations the number of iterations for the hash function
   * @param ngram the gram size
   */
  public record Birthdate(
      @Max(value = 2048) int length,
      HashType fcn0,
      HashType fcn1,
      @Max(value = 50) int iterations,
      @Max(value = 50) int ngram) {}
}
