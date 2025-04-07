package de.gematik.demis.pseudonymization.shared;

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

import java.util.ArrayList;
import java.util.List;

public class NGramGenerator {
  private final int nGramLength;

  public NGramGenerator(final int nGramLength) {
    if (nGramLength < 1) {
      throw new IllegalArgumentException("NGram length must be greater than 0");
    }
    this.nGramLength = nGramLength;
  }

  /**
   * Split the given src string into NGrams of the initialized size
   *
   * @return empty list for empty string
   */
  public List<String> create(final String src) {
    if (src.isEmpty()) {
      return List.of();
    }
    final int requiredNGrams = nGramLength + (src.length() - 1);
    final String padding = " ".repeat(nGramLength - 1);
    final String paddedSrc = padding + src + padding;
    final List<String> result = new ArrayList<>();
    for (int i = 0; i < requiredNGrams; i++) {
      result.add(paddedSrc.substring(i, i + nGramLength));
    }
    return result;
  }
}
