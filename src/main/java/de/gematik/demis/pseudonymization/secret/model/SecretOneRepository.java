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

import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Repository managing {@link SecretOneEntity} entities. */
@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface SecretOneRepository
    extends SecretRepository<SecretOneEntity>, Repository<SecretOneEntity, Integer> {

  // clock_timestamp() -
  // https://www.postgresql.org/docs/14/functions-datetime.html#FUNCTIONS-DATETIME-CURRENT
  @Override
  @Query(
      value =
          """
              INSERT INTO secrets(id, name_fcn_first,
                                  name_fcn_second,
                                  date_fcn_first,
                                  date_fcn_second, created_at)
              VALUES (:id, :name_fcn_first, :name_fcn_second,
                      :date_fcn_first, :date_fcn_second, clock_timestamp());
              """)
  @Modifying
  @Transactional
  void addNewSecret(
      @Param("id") int id,
      @Param("name_fcn_first") String nameFunctionFirst,
      @Param("name_fcn_second") String nameFunctionSecond,
      @Param("date_fcn_first") String dateFunctionFirst,
      @Param("date_fcn_second") String dateFunctionSecond);

  @Override
  @Query(
      value =
          """
      SELECT * FROM secrets
      WHERE created_at::date > now()::date - make_interval(days => :daysOfValidity)
      ORDER BY created_at DESC
      LIMIT 1
      """)
  SecretOneEntity findValidSecret(@Param("daysOfValidity") int daysOfValidity);

  @Override
  @Query(
      value =
          """
      SELECT * FROM secrets
      ORDER BY created_at DESC
      LIMIT 2
      """)
  List<SecretOneEntity> findLastUsedSecrets();

  @Override
  @Query(
      value =
          """
      SELECT * FROM secrets
      ORDER BY created_at DESC
      LIMIT 1
      """)
  SecretOneEntity findLastCreatedSecret();

  @Override
  @Query("LOCK TABLE secrets IN EXCLUSIVE MODE")
  @Modifying
  @Transactional
  void lockTable();
}
