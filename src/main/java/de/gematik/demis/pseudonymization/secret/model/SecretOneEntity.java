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
 * #L%
 */

import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.SECRETS_ONE_TABLE_NAME;

import java.sql.Timestamp;
import org.springframework.data.annotation.Immutable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.validation.annotation.Validated;

/** Entity class for the standard secret, used for non-§7.3 Pseudonyms calculation. */
@Table(name = SECRETS_ONE_TABLE_NAME)
@Validated
@Immutable
public final class SecretOneEntity extends AbstractSecretEntity {

  public SecretOneEntity(
      int id,
      String nameFunctionFirst,
      String nameFunctionSecond,
      String dateFunctionFirst,
      String dateFunctionSecond,
      Timestamp createdTimestamp) {
    super(
        id,
        nameFunctionFirst,
        nameFunctionSecond,
        dateFunctionFirst,
        dateFunctionSecond,
        createdTimestamp);
  }
}
