package de.gematik.demis.pseudonymization.secret;

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

import de.gematik.demis.pseudonymization.secret.load.SecretOneLoaderService;
import de.gematik.demis.pseudonymization.secret.model.SecretOneEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretOneRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

@ExtendWith(MockitoExtension.class)
class SecretOneLoaderServiceIntegrationTest {

  @InjectMocks private SecretOneLoaderService secretLoaderService;

  @Mock private SecretOneRepository secretRepository;

  @BeforeEach
  void beforeEach() {
    Mockito.reset(secretRepository);
  }

  @Test
  void expectLoadFromDatabaseFailsIfEmpty() {
    Mockito.when(secretRepository.findLastUsedSecrets()).thenReturn(List.of());
    Assertions.assertThrows(
        IllegalStateException.class, () -> secretLoaderService.secretPairFromDatabase());
  }

  @Test
  void expectLoadOfSecretsSuccessful() {
    Mockito.when(secretRepository.findLastUsedSecrets())
        .thenReturn(
            List.of(
                // more than 45 days ago
                new SecretOneEntity(
                    1,
                    StringUtils.repeat('A', 50),
                    StringUtils.repeat('B', 50),
                    StringUtils.repeat('C', 50),
                    StringUtils.repeat('D', 50),
                    Timestamp.from(Instant.now().minusSeconds(2000000))),
                new SecretOneEntity(
                    2,
                    StringUtils.repeat('E', 50),
                    StringUtils.repeat('F', 50),
                    StringUtils.repeat('G', 50),
                    StringUtils.repeat('H', 50),
                    Timestamp.from(Instant.now().minusSeconds(10)))));

    final var secretPair =
        Assertions.assertDoesNotThrow(() -> secretLoaderService.secretPairFromDatabase());
    Assertions.assertNotNull(secretPair);
    Assertions.assertNotNull(secretPair.active());
    Assertions.assertNotNull(secretPair.outdated());
    Assertions.assertNotEquals(secretPair.active(), secretPair.outdated());
  }
}
