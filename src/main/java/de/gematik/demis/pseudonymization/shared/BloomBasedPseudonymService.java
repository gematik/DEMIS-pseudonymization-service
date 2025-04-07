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

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/** A service that is able to produce pseudonyms based on the given configuration. */
public class BloomBasedPseudonymService {

  private final BloomFilterFactory bloomFactory;
  private final PseudonymPreprocessingService preprocessingService;
  private final NGramGenerator nGramGenerator;

  /**
   * @param factory A factory that can create new BloomFilter instances
   * @param preprocessingService A preprocessing services that is invoked before building N-Grams
   *     and applying them to the bit vector
   */
  public BloomBasedPseudonymService(
      final BloomFilterFactory factory,
      final PseudonymPreprocessingService preprocessingService,
      final NGramGenerator nGramGenerator) {
    this.bloomFactory = factory;
    this.preprocessingService = preprocessingService;
    this.nGramGenerator = nGramGenerator;
  }

  /**
   * Process the given input without applying configured preprocessing steps. The input is still
   * split into n-grams.
   *
   * @return the base64 encoded pseudonym
   */
  public String processRaw(final String input) {
    // We specifically test for isEmpty() here, because "  " might be the intention, and we can work
    // with that.
    if (input.isEmpty()) {
      return "";
    }
    return toBase64(reduceIntoBloomFilter(splitIntoNGrams(input)));
  }

  /**
   * Process the given input after applying preprocessing steps.
   *
   * @return the base64 encoded pseudonym and potential alternatives
   */
  public List<String> process(final String input) {
    return preprocess(input)
        .map(this::splitIntoNGrams)
        .map(this::reduceIntoBloomFilter)
        .map(this::toBase64)
        .toList();
  }

  private String toBase64(final byte[] input) {
    return new String(Base64.getEncoder().encode(input));
  }

  /**
   * Takes a list of strings (here the NGrams for a transliteration) and applies them to a single
   * bloom filter instance. Then returns the byte array (bit set) of that bloom filter.
   */
  private byte[] reduceIntoBloomFilter(final List<String> src) {
    final BloomFilter bloomFilter = bloomFactory.create();
    src.forEach(bloomFilter::add);
    return bloomFilter.getBitVectorCopy();
  }

  /** Preprocess the input by normalizing and transliterating it */
  private Stream<String> preprocess(final String src) {
    return preprocessingService.preprocess(src).stream();
  }

  private List<String> splitIntoNGrams(final String src) {
    return nGramGenerator.create(src);
  }
}
