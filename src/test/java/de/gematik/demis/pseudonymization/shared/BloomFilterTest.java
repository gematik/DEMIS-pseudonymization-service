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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class BloomFilterTest {

  @Test
  void thatAnExceptionIsThrownIfBitVectorSizeNotDivisibleBy8() {
    final IllegalArgumentException illegalArgumentException =
        catchThrowableOfType(
            IllegalArgumentException.class,
            () -> {
              new BloomFilter.Parameters(1023, null, null, 10);
            });
    assertThat(illegalArgumentException).hasMessage("Bit vector size must be divisible by 8");
  }

  @Test
  void thatAnExceptionIsThrownIfSecondaryHashFunctionIterationsAreSmaller1() {
    final IllegalArgumentException illegalArgumentException =
        catchThrowableOfType(
            IllegalArgumentException.class,
            () -> {
              new BloomFilter.Parameters(1024, null, null, 0);
            });
    assertThat(illegalArgumentException)
        .hasMessage("Number of iterations for secondary hash function must be at least 1");
  }

  @Test
  void addingTheSameElementDoesntChangeTheVector()
      throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("test");
    final byte[] a = sut.getBitVectorCopy();
    sut.add("test");
    final byte[] b = sut.getBitVectorCopy();

    assertThat(a).isEqualTo(b);
  }

  @Test
  void addingDifferentElementsDoesChangeTheVector()
      throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("exam");
    final byte[] a = sut.getBitVectorCopy();
    sut.add("test");
    final byte[] b = sut.getBitVectorCopy();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void dontTruncateBitSet() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("t");
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
  }

  @Test
  void canHandleEmptyString() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("");
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
    assertThat(sut.getBitVectorCopy())
        .isEqualTo(
            new byte[] {
              -126, 32, 8, -126, 32, 8, -126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            });
  }

  @Test
  void canHandleWhiteSpace() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add(" ");
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
  }

  @Test
  void canHandleSpecialCharacter() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("$");
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
  }

  @Test
  void canHandleLongString() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add("a".repeat(1000));
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
  }

  @Test
  void canHandleCharacterWithWhitespace() throws NoSuchAlgorithmException, InvalidKeyException {
    final BloomFilter sut = getSut();
    sut.add(" a");
    assertThat(sut.getBitVectorCopy()).hasSize(512 / 8);
  }

  private static @NotNull BloomFilter getSut()
      throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] bytes = new byte[128];
    bytes[0] = 1;
    SecretKey sha1Key = new SecretKeySpec(bytes, "HmacSHA1");
    SecretKey md5Key = new SecretKeySpec(bytes, "HmacMD5");
    final Mac hmacSha1 = Mac.getInstance("HmacSHA1");
    hmacSha1.init(sha1Key);
    final Mac hmacMd5 = Mac.getInstance("HmacMD5");
    hmacMd5.init(md5Key);
    final BloomFilter.Parameters parameters =
        new BloomFilter.Parameters(512, hmacSha1, hmacMd5, 10);
    return new BloomFilter(parameters);
  }
}
