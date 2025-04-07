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

import com.ibm.icu.text.Transliterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Preprocesses strings for pseudonymization. */
public class PseudonymPreprocessingService {

  public static final Pattern NAME_SEPARATOR = Pattern.compile("[ ,-]");

  private final List<Transliterator> transliterators;

  /**
   * @param transliterators Assign a copy of configured transliterators to this service
   */
  public PseudonymPreprocessingService(final List<Transliterator> transliterators) {
    this.transliterators = List.copyOf(transliterators);
  }

  /**
   * Preprocess an input according to the pseudonymization requirements:
   *
   * <ol>
   *   <li>split the input into multiple parts based on whitespace and punctuation
   *   <li>transliterate non-ASCII characters into similar sounding ASCII characters
   *   <li>remove remaining non-ASCII characters
   *   <li>transpose to upper-case characters
   * </ol>
   *
   * <h4>Handling duplicates in the transliteration result</h4>
   *
   * The current implementation for transliteration is based on ICU4J. {@link
   * com.ibm.icu.text.Transliterator#transliterate(String)} can only return a single string. To
   * ensure backwards-compatibility we occasionally need to return multiple transliterations:
   *
   * <pre>  Müller -> Muller, Mueller</pre>
   *
   * To achieve this we apply two Transliterators with slightly different configurations. That will
   * lead to duplicates being created when the configurations lead to the same result:
   *
   * <pre>  foobar -> foobar, foobar</pre>
   *
   * We only need the first distinct value, therefore we discard the second <code>foobar</code>
   * result. Now, imagine a name like:
   *
   * <pre>  Mũller-Müller -> Muller, Mueller, Muller, Mueller</pre>
   *
   * Here we want to preserve the semantics of the name, and we can't discard the duplicates in the
   * final result. So returning <code>Muller, Mueller</code> would be incorrect, as it only reflects
   * the first `Mũller`.
   *
   * @return the resulting preprocessed parts that are not blank
   */
  public List<String> preprocess(final String part) {
    return split(part)
        .flatMap(this::transliterateToASCII)
        .map(this::removeSpecialCharacters)
        .filter(Predicate.not(String::isBlank))
        .map(this::toUpperCase)
        .toList();
  }

  private Stream<String> split(final String part) {
    return Arrays.stream(NAME_SEPARATOR.split(part))
        .map(String::trim)
        .filter(Predicate.not(String::isBlank));
  }

  private Stream<String> transliterateToASCII(final String src) {
    /*
    When multiple transliterators lead to the same result
    we can remove the duplicates using distinct().
     */
    return transliterators.stream().map(t -> t.transliterate(src)).distinct();
  }

  private String removeSpecialCharacters(final String src) {
    final StringBuilder result = new StringBuilder();
    final char[] charArray = src.toCharArray();
    for (char c : charArray) {
      if (Character.isLetter(c)) {
        result.append(c);
      }
    }

    return result.toString();
  }

  /**
   * Convert the given source string to upper-case. This assumes that we have already removed all
   * non-ASCII characters that might lead to problems when converting to upper-case.
   */
  private String toUpperCase(final String src) {
    return src.toUpperCase(Locale.ROOT);
  }
}
