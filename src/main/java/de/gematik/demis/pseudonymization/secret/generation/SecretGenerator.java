package de.gematik.demis.pseudonymization.secret.generation;

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

import java.security.SecureRandom;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/** Utility to generate a new secret String, that is part of a Secret Entity. */
@Slf4j
public class SecretGenerator {
  private final Random secureRandom = new SecureRandom();
  private final char[] symbols;
  private final int length;

  public SecretGenerator(char[] symbols, int length) {
    this.symbols = symbols;
    this.length = length;
  }

  /**
   * Generates a new random secret.
   *
   * @return the Secret as String
   */
  public String getRandomSecret() {
    StringBuilder secret = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      secret.append(symbols[secureRandom.nextInt(symbols.length)]);
    }
    return secret.toString();
  }
}
