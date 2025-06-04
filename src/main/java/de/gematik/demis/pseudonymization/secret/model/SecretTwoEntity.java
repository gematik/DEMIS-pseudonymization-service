package de.gematik.demis.pseudonymization.secret.model;

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

import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.SECRETS_TWO_TABLE_NAME;

import java.sql.Timestamp;
import org.springframework.data.annotation.Immutable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.validation.annotation.Validated;

/** Entity class for the extended secret, used for §7.3 Pseudonyms calculation. */
@Table(name = SECRETS_TWO_TABLE_NAME)
@Validated
@Immutable
public final class SecretTwoEntity extends AbstractSecretEntity {

  public SecretTwoEntity(
      final int id,
      final String nameFunctionFirst,
      final String nameFunctionSecond,
      final String dateFunctionFirst,
      final String dateFunctionSecond,
      final Timestamp createdTimestamp) {
    super(
        id,
        nameFunctionFirst,
        nameFunctionSecond,
        dateFunctionFirst,
        dateFunctionSecond,
        createdTimestamp);
  }
}
