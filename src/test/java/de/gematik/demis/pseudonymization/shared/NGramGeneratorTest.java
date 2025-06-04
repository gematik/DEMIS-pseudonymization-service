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

import org.junit.Test;

public class NGramGeneratorTest {

  @Test
  public void canDealWithEmptyString() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("")).isEmpty();
  }

  @Test
  public void splitsEvenStringsCorrectly() {
    NGramGenerator nGramGenerator = new NGramGenerator(5);
    assertThat(nGramGenerator.create("test"))
        .containsExactly("    t", "   te", "  tes", " test", "test ", "est  ", "st   ", "t    ");

    nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("test"))
        .containsExactly("  t", " te", "tes", "est", "st ", "t  ");

    nGramGenerator = new NGramGenerator(2);
    assertThat(nGramGenerator.create("test")).containsExactly(" t", "te", "es", "st", "t ");

    nGramGenerator = new NGramGenerator(1);
    assertThat(nGramGenerator.create("test")).containsExactly("t", "e", "s", "t");
  }

  @Test
  public void splitsUnevenStringsCorrectly() {
    NGramGenerator nGramGenerator = new NGramGenerator(5);
    assertThat(nGramGenerator.create("tes"))
        .containsExactly("    t", "   te", "  tes", " tes ", "tes  ", "es   ", "s    ");

    nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("tes")).containsExactly("  t", " te", "tes", "es ", "s  ");

    nGramGenerator = new NGramGenerator(2);
    assertThat(nGramGenerator.create("tes")).containsExactly(" t", "te", "es", "s ");

    nGramGenerator = new NGramGenerator(1);
    assertThat(nGramGenerator.create("tes")).containsExactly("t", "e", "s");
  }

  @Test
  public void nGramLargerThanInput() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("t")).containsExactly("  t", " t ", "t  ");
  }

  @Test
  public void nGramNoInput() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("")).isEmpty();
  }

  @Test
  public void nGramSingleCharacter() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("a")).containsExactly("  a", " a ", "a  ");
  }

  @Test
  public void nGramWhitespace() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create(" ")).containsExactly("   ", "   ", "   ");
  }

  @Test
  public void nGramNewLine() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("\n")).containsExactly("  \n", " \n ", "\n  ");
  }

  @Test
  public void nGramSpecialCharacter() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("$")).containsExactly("  $", " $ ", "$  ");
  }

  @Test
  public void nGramInteger() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("1")).containsExactly("  1", " 1 ", "1  ");
  }

  @Test
  public void nGramStringWithWhitespace() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create(" a")).containsExactly("   ", "  a", " a ", "a  ");
  }

  @Test
  public void nGramDate() {
    final NGramGenerator nGramGenerator = new NGramGenerator(3);
    assertThat(nGramGenerator.create("01.01.1970"))
        .containsExactly(
            "  0", " 01", "01.", "1.0", ".01", "01.", "1.1", ".19", "197", "970", "70 ", "0  ");
  }

  @Test
  public void throwsExceptionForIllegalArguments() {
    IllegalArgumentException illegalArgumentException =
        catchThrowableOfType(
            IllegalArgumentException.class,
            () -> {
              new NGramGenerator(-1);
            });
    assertThat(illegalArgumentException).isNotNull();

    illegalArgumentException =
        catchThrowableOfType(
            IllegalArgumentException.class,
            () -> {
              new NGramGenerator(0);
            });
    assertThat(illegalArgumentException).isNotNull();
  }
}
