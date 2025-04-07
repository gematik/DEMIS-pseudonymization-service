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
 * #L%
 */

import de.gematik.demis.pseudonymization.secret.model.SecretOneEntity;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityConverterTest {
  @Test
  void expectConversionSuccessful() {
    // GIVEN
    SecretOneEntity standardSecretEntity =
        new SecretOneEntity(1, "AAA", "BBB", "CCC", "DDD", Timestamp.from(Instant.now()));
    // WHEN
    final var convertedObject = EntityConverter.fromDatabaseEntity(standardSecretEntity);
    // THEN
    Assertions.assertEquals(
        convertedObject.birthdateFirstFunction(), standardSecretEntity.getDateFunctionFirst());
    Assertions.assertEquals(
        convertedObject.birthdateSecondFunction(), standardSecretEntity.getDateFunctionSecond());
    Assertions.assertEquals(
        convertedObject.nameFirstFunction(), standardSecretEntity.getNameFunctionFirst());
    Assertions.assertEquals(
        convertedObject.nameSecondFunction(), standardSecretEntity.getNameFunctionSecond());
    Assertions.assertNotNull(convertedObject.createdAt());
  }
}
