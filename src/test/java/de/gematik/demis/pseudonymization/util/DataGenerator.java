package de.gematik.demis.pseudonymization.util;

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

import de.gematik.demis.pseudonymization.config.HashType;
import de.gematik.demis.pseudonymization.config.PseudonymFilterConfiguration;
import org.springframework.http.HttpHeaders;

public final class DataGenerator {

  public static final String LEGACY_CONTENT_TYPE = "application/vnd.demis_pseudonymization+json";

  public static final String NEW_CONTENT_TYPE = "application/json";

  private static final String DRIVER_NAME = "de.fraunhofer.fokus.adep.pseudonymization.Bloom";

  private DataGenerator() {}

  public static HttpHeaders getDefaultHeaderWithContentType(
      final String contentType, final String acceptContent) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, contentType);
    headers.set(HttpHeaders.ACCEPT, acceptContent);
    return headers;
  }

  public static HttpHeaders getDefaultHeader(boolean useLegacyContentType) {
    HttpHeaders headers = new HttpHeaders();
    if (useLegacyContentType) {
      headers.set(HttpHeaders.CONTENT_TYPE, LEGACY_CONTENT_TYPE);
      headers.set(HttpHeaders.ACCEPT, LEGACY_CONTENT_TYPE);
    } else {
      headers.set(HttpHeaders.CONTENT_TYPE, NEW_CONTENT_TYPE);
      headers.set(HttpHeaders.ACCEPT, NEW_CONTENT_TYPE);
    }
    return headers;
  }

  public static PseudonymFilterConfiguration getFilterConfiguration() {
    final var nameConfig =
        new PseudonymFilterConfiguration.Name(1024, HashType.HMAC_SHA1, HashType.HMAC_MD5, 10, 3);
    final var dateConfig =
        new PseudonymFilterConfiguration.Birthdate(
            1024, HashType.HMAC_SHA1, HashType.HMAC_MD5, 10, 3);
    return new PseudonymFilterConfiguration(DRIVER_NAME, nameConfig, dateConfig);
  }
}
