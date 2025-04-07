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

import de.gematik.demis.pseudonymization.shared.BloomBasedPseudonymService;
import de.gematik.demis.pseudonymization.shared.BloomFilter;
import de.gematik.demis.pseudonymization.shared.BloomFilterFactory;
import de.gematik.demis.pseudonymization.shared.NGramGenerator;
import de.gematik.demis.pseudonymization.shared.PseudonymPreprocessingService;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * A wrapper around {@link BloomFilterFactory} and {@link BloomBasedPseudonymService} that takes
 * default configuration parameters like vector size, hash function name etc. and creates a
 * correctly configured {@link BloomBasedPseudonymService} instance.
 */
public class BloomBasedPseudonymServiceFactory {

  private final int bitVectorSize;
  private final int iterations;
  private final String primaryHashFunctionName;
  private final String secondaryHashFunctionName;
  private final NGramGenerator nGramGenerator;
  private final PseudonymPreprocessingService preprocessingService;

  /**
   * @param bitVectorSize The size to use when creating new bloom filters
   * @param iterations The number of iterations to apply the second hash function for
   */
  public BloomBasedPseudonymServiceFactory(
      final PseudonymPreprocessingService preprocessingService,
      final int bitVectorSize,
      final int iterations,
      final String primaryHashFunction,
      final String secondaryHashFunction,
      final int nGramLength) {
    this.bitVectorSize = bitVectorSize;
    this.iterations = iterations;
    this.primaryHashFunctionName = primaryHashFunction;
    this.secondaryHashFunctionName = secondaryHashFunction;
    this.nGramGenerator = new NGramGenerator(nGramLength);
    this.preprocessingService = preprocessingService;
  }

  private Mac initializeHashFunction(final String name, final String key)
      throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKey hashFunctionKey = new SecretKeySpec(key.getBytes(), name);
    final Mac hashFunction = Mac.getInstance(name);
    hashFunction.init(hashFunctionKey);
    return hashFunction;
  }

  /**
   * Create a new bloom based pseudonym service using the given secrets and preconfigured parameters
   */
  public BloomBasedPseudonymService create(
      final String primarySecret, final String secondarySecret) {
    try {
      final Mac primaryHashFunction =
          initializeHashFunction(primaryHashFunctionName, primarySecret);
      final Mac secondaryHashFunction =
          initializeHashFunction(secondaryHashFunctionName, secondarySecret);
      final BloomFilter.Parameters parameters =
          new BloomFilter.Parameters(
              bitVectorSize, primaryHashFunction, secondaryHashFunction, iterations);
      final BloomFilterFactory bloomFilterFactory = new BloomFilterFactory(parameters);
      return new BloomBasedPseudonymService(
          bloomFilterFactory, preprocessingService, nGramGenerator);
    } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }
}
