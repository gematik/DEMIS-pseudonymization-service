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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import javax.crypto.Mac;

/**
 * Represents a bit vector of size N. Bits can be flipped based on the result of applying the
 * configured hash functions to an input. This implementation does not offer a `contains`
 * implementation, as it's not needed for our use case.
 */
public class BloomFilter {
  /** Parameters used by a BloomFilter to apply inputs to hash functions. */
  public record Parameters(
      int bitVectorSize,
      Mac primaryHashFunction,
      Mac secondaryHashFunction,
      int secondaryHashFunctionIterations) {

    /**
     * @throws IllegalArgumentException when bit vector size is not divisible by 8 OR when number of
     *     iterations for secondary hash function is < 1
     */
    public Parameters {
      if (bitVectorSize % 8 != 0) {
        throw new IllegalArgumentException("Bit vector size must be divisible by 8");
      }
      if (secondaryHashFunctionIterations < 1) {
        throw new IllegalArgumentException(
            "Number of iterations for secondary hash function must be at least 1");
      }
    }
  }

  private final Parameters parameters;
  private final BitSet bitVector;
  // We are just trying to avoid creating unnecessary objects in loops here
  private final BigInteger bitVectorSize;

  public BloomFilter(final Parameters parameters) {
    this.parameters = parameters;
    bitVector = new BitSet(parameters.bitVectorSize());
    bitVectorSize = BigInteger.valueOf(bitVector.size());
  }

  /** Apply the given input to the bloom filter */
  public void add(final byte[] input) {
    final byte[] hash1 = parameters.primaryHashFunction().doFinal(input);
    final byte[] hash2 = parameters.secondaryHashFunction().doFinal(input);

    final BigInteger primaryHash = new BigInteger(1, hash1);
    final BigInteger secondaryHash = new BigInteger(1, hash2);

    for (int i = 0; i < parameters.secondaryHashFunctionIterations(); i++) {
      final BigInteger iterationResult =
          secondaryHash.multiply(BigInteger.valueOf(i)).add(primaryHash);
      final int iterationTargetIndex = iterationResult.mod(bitVectorSize).intValue();
      bitVector.set(iterationTargetIndex);
    }
  }

  /**
   * Convenience method that converts the given string into a byte array before applying it to the
   * bit vector.
   */
  public void add(final String input) {
    this.add(input.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * @return an independent copy of the bit vector at call time
   */
  public byte[] getBitVectorCopy() {
    // BitSet will remove trailing bits set to 0 in the returned ByteArray.
    // By using copyOf we just fill up these empty spots and avoid the truncating.
    return Arrays.copyOf(bitVector.toByteArray(), bitVector.size() / 8);
  }
}
