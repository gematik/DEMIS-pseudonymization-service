package de.gematik.demis.pseudonymization.secret.load;

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

import de.gematik.demis.pseudonymization.secret.model.SecretOneEntity;
import de.gematik.demis.pseudonymization.secret.model.SecretOneRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/** Service responsible for loading {@link SecretOneEntity} from the database. */
@Slf4j
@Service
public class SecretOneLoaderService extends SecretLoaderService<SecretOneEntity> {

  public SecretOneLoaderService(final SecretOneRepository secretRepository) {
    super(secretRepository);
  }

  @Override
  public Logger log() {
    return log;
  }
}
