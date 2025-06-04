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

import com.ibm.icu.text.Transliterator;
import de.gematik.demis.pseudonymization.config.BloomPseudonymConfiguration;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

public class BloomBasedPseudonymServiceTest {

  private BloomFilterFactory bloomFilterFactory() {
    try {
      byte[] weakSecret = new byte[64];
      weakSecret[1] = (byte) 0x80;
      SecretKey sha1Key = new SecretKeySpec(weakSecret, "HmacSHA1");
      SecretKey md5Key = new SecretKeySpec(weakSecret, "HmacMD5");
      final Mac hmacSha1 = Mac.getInstance("HmacSHA1");
      hmacSha1.init(sha1Key);
      final Mac hmacMd5 = Mac.getInstance("HmacMD5");
      hmacMd5.init(md5Key);
      // 64 results in shorter strings and makes the tests more readable
      return new BloomFilterFactory(new BloomFilter.Parameters(128, hmacSha1, hmacMd5, 10));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  private BloomBasedPseudonymService sut() {
    final PseudonymPreprocessingService preprocessingService =
        new PseudonymPreprocessingService(
            List.of(
                BloomPseudonymConfiguration.gematikTransliterator(),
                // This configuration leads to umlauts being replaced with two characters (ü -> ue),
                // Any-Latin is still required to transliterate Cyrillic properly
                Transliterator.getInstance("Any-Latin; de-ASCII"),
                // This configuration leads to umlauts being replaced with a single characters (ü ->
                // u)
                Transliterator.getInstance(
                    "Any-Latin; NFD; [:Nonspacing Mark:] Remove; NFC; Latin-ASCII")));
    return new BloomBasedPseudonymService(
        bloomFilterFactory(), preprocessingService, new NGramGenerator(3));
  }

  @Test
  void thatSimplePseudonymizationWorks() {
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("family");
    assertThat(result).containsExactlyInAnyOrder("J6UJmJhYaVXE25AplGzczg==");
  }

  @Test
  void thatMultipleTransliterationsAreHandled() {
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    // GIVEN an input that will result in multiple transliterations (i.e. Müller -> [Muller,
    // Mueller])
    final List<String> result = bloomBasedPseudonymService.process("meißen-müller");
    // THEN each transliteration is added to the result
    assertThat(result)
        .containsExactlyInAnyOrder(
            "OqXEhjBS2ruTxuC41oOhoQ==", "GyPA5jHN6YGGqavhk76tvQ==", "GyPAoiG946ihrZuDk6Knuw==");
    /*
    The result above can be explained as follows:
    1. Split the input at the hyphen:
      [meißen, müller]
    2. transliterate each element and bring them back together:
      [meissen, mueller, muller]
    3. proceed as usual
     */
  }

  @Test
  void thatRawProcessingWorks() {
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final String result = bloomBasedPseudonymService.processRaw("01.01.1970");
    assertThat(result).isEqualTo("ibmLmo83CzOz9u9jduzpjQ==");
  }

  @Test
  void thatEmptyStringsAreHandled() {
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final String result = bloomBasedPseudonymService.processRaw("");
    assertThat(result).isEqualTo("");

    final List<String> results = bloomBasedPseudonymService.process("");
    assertThat(results).isEmpty();
  }

  @Test
  void thatPseudonymizationWorksSpace() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> results = bloomBasedPseudonymService.process(" ");
    assertThat(results).isEmpty();
  }

  @Test
  void thatPseudonymizationWorksTab() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> results = bloomBasedPseudonymService.process("\t");
    assertThat(results).isEmpty();
  }

  @Test
  void thatPseudonymizationWorksNewLine() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> results = bloomBasedPseudonymService.process("\n");
    assertThat(results).isEmpty();
  }

  @Test
  void thatPseudonymizationWorksSpecialCharacters() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> results = bloomBasedPseudonymService.process("\"!@#$%^&*()");
    assertThat(results).isEmpty();
  }

  @Test
  void thatPseudonymizationWorksSingleCharacter() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("a");
    assertThat(result).containsExactlyInAnyOrder("mAAiIgKQmQEkIIgJCGBGAg==");
  }

  @Test
  void thatPseudonymizationWorksSingleCharacterWithWhitespace() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process(" a");
    assertThat(result).containsExactlyInAnyOrder("mAAiIgKQmQEkIIgJCGBGAg==");
  }

  @Test
  void thatPseudonymizationWorksSingleCharacterWithDash() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("-a");
    assertThat(result).containsExactlyInAnyOrder("mAAiIgKQmQEkIIgJCGBGAg==");
  }

  @Test
  void thatPseudonymizationWorksSingleCharacterWithDot() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process(".a");
    assertThat(result).containsExactlyInAnyOrder("mAAiIgKQmQEkIIgJCGBGAg==");
  }

  @Test
  void thatPseudonymizationWorksLongString() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("a".repeat(1_000));
    // Here we expect the same result, becuase 'aaaa....n' will be transliterated to 'aaaa...n/2'
    // and end up
    // generating the same pseudonym. This is due to the test data and not really a bug.
    assertThat(result)
        .containsExactlyInAnyOrder("iMQiKkSImWHtkuwNCKBOgA==", "iMQiKkSImWHtkuwNCKBOgA==");
  }

  @Test
  void thatPseudonymizationDoesNotWorkWithYearMonth() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("01.01.1970");
    assertThat(result).containsExactlyInAnyOrder();
  }

  @Test
  void thatPseudonymizationDoesNotWorkYearMonthDay() { // edge case 1
    final BloomBasedPseudonymService bloomBasedPseudonymService = sut();
    final List<String> result = bloomBasedPseudonymService.process("FooBar01.01.1970");
    assertThat(result).containsExactlyInAnyOrder("i6URsiG8SIm0Q9lfdyEfUA==");
  }
}
