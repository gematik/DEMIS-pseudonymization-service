package de.gematik.demis.pseudonymization;

/*-
 * #%L
 * FHIR UI Data Model Translation Service
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

import de.gematik.demis.AbstractOpenApiSpecDownloaderTest;
import de.gematik.demis.pseudonymization.util.SpringTestContainerStarter;
import org.junit.jupiter.api.Nested;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "test")
class OpenApiSpecDownloaderTest extends SpringTestContainerStarter {
  @Nested
  class InnerOpenApiSpecDownloaderTest extends AbstractOpenApiSpecDownloaderTest {
    // executes test from AbstractOpenApiSpecDownloaderTest
  }
}
