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
import java.util.List;
import org.junit.Test;

public class PseudonymPreprocessingServiceTest {

  @Test
  public void canDealEmptyString() {
    // Regular {waving hand}
    assertThat(sut().preprocess("")).isEmpty();
  }

  @Test
  public void canDealWithWhitespaceOnly() {
    assertThat(sut().preprocess(" ")).isEmpty();
  }

  @Test
  public void canDealWithTab() {
    assertThat(sut().preprocess("\t")).isEmpty();
  }

  @Test
  public void canDealWithNewLine() {
    assertThat(sut().preprocess("\n")).isEmpty();
  }

  @Test
  public void canDealSpecialCharacter() {
    assertThat(sut().preprocess("$")).isEmpty();
  }

  @Test
  public void canDealWithInt() {
    assertThat(sut().preprocess("1")).isEmpty();
  }

  @Test
  public void canDealWithLeadingWhitespace() {
    assertThat(sut().preprocess(" a")).containsExactly("A");
  }

  @Test
  public void canDealWithLeadingDash() {
    assertThat(sut().preprocess("-a")).containsExactly("A");
  }

  @Test
  public void canDealWithDate() {
    assertThat(sut().preprocess("01.1970")).containsExactly();
  }

  @Test
  public void canDealWithDateAndLetters() {
    assertThat(sut().preprocess("FooBar01.1970")).containsExactly("FOOBAR");
  }

  public void canDealWithEmoji() {
    // Regular {waving hand}
    assertThat(sut().preprocess("Regular \uD83D\uDC4B")).containsExactly("REGULAR");
    assertThat(sut().preprocess("Regular\uD83D\uDC4B")).containsExactly("REGULAR");
  }

  @Test
  public void canDealWithAllKindsOfHyphens() {
    assertThat(sut().preprocess("A minussign-")).containsExactly("A", "MINUSSIGN");
    assertThat(sut().preprocess("A minussign---minussign"))
        .containsExactly("A", "MINUSSIGN", "MINUSSIGN");

    // The following are unsupported during the split-phase, we are recording the status-quo of the
    // implementation to
    // notice if the implementation detail changes
    assertThat(sut().preprocess("A non\u2011breaking-hyphen"))
        .containsExactly("A", "NONBREAKING", "HYPHEN");
    assertThat(sut().preprocess("An en\u2013dash")).containsExactly("AN", "ENDASH");
    assertThat(sut().preprocess("An em\u2014dash")).containsExactly("AN", "EMDASH");
    assertThat(sut().preprocess("A soft\u00adhyphen")).containsExactly("A", "SOFTHYPHEN");
  }

  @Test
  public void canDealWithWhitespace() {
    assertThat(sut().preprocess("Regular Whitespace")).containsExactly("REGULAR", "WHITESPACE");
    assertThat(sut().preprocess("Just  some extra  whitespace"))
        .containsExactly("JUST", "SOME", "EXTRA", "WHITESPACE");

    // The following are unsupported during the split-phase, we are recording the status-quo of the
    // implementation to
    // notice if the implementation detail changes
    assertThat(sut().preprocess("Add tabs\t here")).containsExactly("ADD", "TABS", "HERE");
    assertThat(sut().preprocess("nonbreaking\u00a0space")).containsExactly("NONBREAKINGSPACE");
    assertThat(sut().preprocess("em\u2002quad")).containsExactly("EMQUAD");
  }

  @Test
  public void canDealWithSpecialCharacters() {
    assertThat(sut().preprocess("Regular $Joe")).containsExactlyInAnyOrder("REGULAR", "JOE");
    assertThat(sut().preprocess("Regular \\Joe")).containsExactlyInAnyOrder("REGULAR", "JOE");
    assertThat(sut().preprocess("Regular O'Neil"))
        .containsExactlyInAnyOrder("REGULAR", "ONEIL", "NEIL");
    assertThat(sut().preprocess("Regular $")).containsExactlyInAnyOrder("REGULAR");

    /*
     * Joe should only be transliterated to "JOE". Here we get "JOE", "JO". That's because the gematik transliterator
     * matches "end of line". Here the line is not finished, because special characters follow. Special characters are
     * removed after transliteration. This order simplifies special character removal (anything not transliterated
     * proplery will go away, unless it's a character according to unicode). This behaviour might be fixable with
     * specific rules (perhaps using anchors?). For now, we accept it as is.
     */
    assertThat(sut().preprocess("Regular Joe!@#$%^&*()~"))
        .containsExactlyInAnyOrder("REGULAR", "JOE", "JO");
  }

  @Test
  public void asciiStaysIntact() {
    assertThat(sut().preprocess("John Doe")).containsExactly("JOHN", "DOE");
  }

  @Test
  public void canTransliterateWithHyphens() {
    assertThat(sut().preprocess("Leutheuser-Schnarrenberger"))
        .containsExactly("LEUTHEUSER", "SCHNARRENBERGER");
  }

  @Test
  public void canDealWithApostrophe() {
    assertThat(sut().preprocess("O'Haire")).containsExactlyInAnyOrder("OHAIRE", "HAIRE");
  }

  @Test
  public void reducesAAIntoA() {
    assertThat(sut().preprocess("Haas")).containsExactlyInAnyOrder("HAAS", "HAS");
    assertThat(sut().preprocess("Haaaas")).containsExactlyInAnyOrder("HAAAAS", "HAAS");
    assertThat(sut().preprocess("Haaaaas")).containsExactlyInAnyOrder("HAAAS", "HAAAAAS");
  }

  @Test
  public void canTransliterateCyrillic() {
    assertThat(sut().preprocess("Иван Минчов Вазов")).containsExactly("IVAN", "MINCOV", "VAZOV");
  }

  @Test
  public void canTransliterateGreek() {
    assertThat(sut().preprocess("Πλάτων")).containsExactly("PLATON");
  }

  @Test
  public void canTransliterateAccents() {
    assertThat(sut().preprocess("Pierré")).containsExactly("PIERRE");
    assertThat(sut().preprocess("Pierrè")).containsExactly("PIERRE");
  }

  @Test
  public void canTransliterateVirgulilla() {
    // multiple transliterations are possible depending on the source language, we encode it like
    // this for now
    assertThat(sut().preprocess("Piñata")).containsExactly("PINATA");
  }

  @Test
  public void canTransliterateHangul() {
    // https://en.wikipedia.org/wiki/Park_(Korean_surname)
    assertThat(sut().preprocess("박")).contains("BAG");
    // NOTE: initially transliterated CHAN-UG, however we are removing special characters, so the
    // result is CHANUG.
    assertThat(sut().preprocess("찬욱")).containsExactly("CHANUG");
  }

  @Test
  public void canTransliterateUmlauts() {
    // capital and lower case ß
    assertThat(sut().preprocess("ßß")).containsExactlyInAnyOrder("SSSS");
    assertThat(sut().preprocess("MÖLLER")).containsExactlyInAnyOrder("MOLLER", "MOELLER");
    assertThat(sut().preprocess("Müller")).containsExactlyInAnyOrder("MULLER", "MUELLER");
    // Ensure we are not greedily removing duplicates and preserve semantics here
    assertThat(sut().preprocess("Müller-Müller"))
        .containsExactlyInAnyOrder("MULLER", "MUELLER", "MULLER", "MUELLER");
    assertThat(sut().preprocess("Müller-Müller"))
        .containsExactlyInAnyOrder("MULLER", "MUELLER", "MULLER", "MUELLER");
    // note we are using a ũ(u+tilde) here, not an ü!
    assertThat(sut().preprocess("Mũller-Müller"))
        .containsExactlyInAnyOrder("MULLER", "MULLER", "MUELLER");
  }

  @Test
  public void handlesAeOeUeCorrectly() {
    final PseudonymPreprocessingService sut = sut();
    assertThat(sut.preprocess("Üeli"))
        .containsExactlyInAnyOrder("UELI", "UEELI"); // Ü -> (UE, U) + eli
    assertThat(sut.preprocess("Jül")).containsExactlyInAnyOrder("JUEL", "JUL");
    assertThat(sut.preprocess("Ueli")).containsExactlyInAnyOrder("ULI", "UELI");
    assertThat(sut.preprocess("UEli"))
        .containsExactlyInAnyOrder("ULI", "UELI"); // incorrectly capitalised
    assertThat(sut.preprocess("Üli")).containsExactlyInAnyOrder("ULI", "UELI");
    assertThat(sut.preprocess("Loeffel")).containsExactlyInAnyOrder("LOEFFEL", "LOFFEL");
    assertThat(sut.preprocess("Löffel")).containsExactlyInAnyOrder("LOEFFEL", "LOFFEL");
    assertThat(sut.preprocess("Loeoeffel")).containsExactlyInAnyOrder("LOEOEFFEL", "LOOFFEL");

    // Ensure we don't convert too much at the end
    assertThat(sut.preprocess("Joe")).containsExactly("JOE");
    assertThat(sut.preprocess("Joe-")).containsExactly("JOE");
    assertThat(sut.preprocess("Joe Joe")).containsExactly("JOE", "JOE");
    assertThat(sut.preprocess("Joel")).containsExactlyInAnyOrder("JOEL", "JOL");
    assertThat(sut.preprocess("oel")).containsExactlyInAnyOrder("OEL", "OL");
    assertThat(sut.preprocess("Jae")).containsExactly("JAE");
    assertThat(sut.preprocess("Jael")).containsExactlyInAnyOrder("JAEL", "JAL");
    assertThat(sut.preprocess("ael")).containsExactlyInAnyOrder("AEL", "AL");
    assertThat(sut.preprocess("Jue")).containsExactly("JUE");
    assertThat(sut.preprocess("Juel")).containsExactlyInAnyOrder("JUEL", "JUL");
    assertThat(sut.preprocess("uel")).containsExactlyInAnyOrder("UEL", "UL");
    assertThat(sut.preprocess("Jü")).containsExactlyInAnyOrder("JUE", "JU");
  }

  private PseudonymPreprocessingService sut() {
    return new PseudonymPreprocessingService(
        List.of(
            BloomPseudonymConfiguration.gematikTransliterator(),
            // This configuration leads to umlauts being replaced with two characters (ü -> ue),
            // Any-Latin is still required to transliterate Cyrillic properly
            Transliterator.getInstance("Any-Latin; de-ASCII; Latin-ASCII"),
            // This configuration leads to umlauts being replaced with a single characters (ü -> u)
            Transliterator.getInstance(
                "Any-Latin; NFD; [:Nonspacing Mark:] Remove; NFC; Latin-ASCII")));
  }
}
