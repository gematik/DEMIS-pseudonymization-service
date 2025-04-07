package de.gematik.demis.pseudonymization.config;

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
import de.gematik.demis.pseudonymization.core.BloomBasedPseudonymServiceFactory;
import de.gematik.demis.pseudonymization.shared.PseudonymPreprocessingService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects the bloom based pseudonym services with the configuration parameters obtained by Spring.
 */
@Configuration
public class BloomPseudonymConfiguration {

  private static final String GEMATIK_TRANSLITERATOR_ID = "any-ascii_gematik";

  /** NOTE: this method is used by tests that can't rely on Spring */
  public static Transliterator gematikTransliterator() {
    /*
    The order of rules and ids here is important. If any-ASCII is put first, then umlauts will be translated first
    and then we are running the custom umlaut conversion. Incorrect results are produced, e.g. for "Üeli":
    1. Üeli -> Ueli, 2. Ueli -> ULI
    If we are using de-ASCII instead, then we can't handle non-latin characters (e.g. cyrillic, hangul, ...), this
    configuration keeps the number of transliteration rules smallest.
    */
    return Transliterator.createFromRules(
        GEMATIK_TRANSLITERATOR_ID,
        """
            ^ O\\' >;
            aa > a;
            ue $ > ue;
            Ue>U;UE>U;ue>u;
            oe $ > oe;
            Oe>O;OE>O;oe>o;
            ae $ > ae;
            Ae>A;AE>A;ae>a;
            ::any-Latin;
            ::Latin-ASCII;
            """,
        Transliterator.FORWARD);
  }

  public static final String BIRTHDAY_PSEUDONYM_SERVICE_FACTORY = "birthdayPseudonymServiceFactory";
  public static final String NAME_PSEUDONYM_SERVICE_FACTORY = "namePseudonymServiceFactory";

  @Bean
  public PseudonymPreprocessingService pseudonymPreprocessingService(
      @Value("${pseudonym.preprocessing.transliteratorIds}") final String rawTransliteratorIds) {
    final String[] transliteratorIds = rawTransliteratorIds.split("\n");
    List<Transliterator> transliterators = new ArrayList<>(transliteratorIds.length + 1);
    transliterators.add(gematikTransliterator());
    Stream.of(transliteratorIds).map(Transliterator::getInstance).forEach(transliterators::add);
    return new PseudonymPreprocessingService(transliterators);
  }

  @Bean(name = NAME_PSEUDONYM_SERVICE_FACTORY)
  public BloomBasedPseudonymServiceFactory namePseudonymServiceFactory(
      final PseudonymPreprocessingService preprocessingService,
      final PseudonymFilterConfiguration configuration) {
    return new BloomBasedPseudonymServiceFactory(
        preprocessingService,
        configuration.name().length(),
        configuration.birthdate().iterations(),
        configuration.name().fcn0().value(),
        configuration.name().fcn1().value(),
        configuration.name().ngram());
  }

  @Bean(name = BIRTHDAY_PSEUDONYM_SERVICE_FACTORY)
  public BloomBasedPseudonymServiceFactory birthdayPseudonymServiceFactory(
      final PseudonymPreprocessingService preprocessingService,
      final PseudonymFilterConfiguration configuration) {
    return new BloomBasedPseudonymServiceFactory(
        preprocessingService,
        configuration.birthdate().length(),
        configuration.birthdate().iterations(),
        configuration.birthdate().fcn0().value(),
        configuration.birthdate().fcn1().value(),
        configuration.birthdate().ngram());
  }
}
