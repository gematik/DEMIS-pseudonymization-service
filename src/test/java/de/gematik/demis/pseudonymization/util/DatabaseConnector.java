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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

/** JDBC Connector to test if data can be read from DB. */
@Slf4j
public class DatabaseConnector {
  private final JdbcTemplate jdbcTemplate;

  public DatabaseConnector(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void cleanSecretOneEntries() {
    this.jdbcTemplate.execute("TRUNCATE TABLE secrets");
  }

  public void cleanSecretTwoEntries() {
    this.jdbcTemplate.execute("TRUNCATE TABLE secrets_two");
  }

  public void writeInitialSecretOneEntries() {
    this.jdbcTemplate.execute(
        """
            INSERT INTO secrets(name_fcn_first,
                                name_fcn_second,
                                date_fcn_first,
                                date_fcn_second, created_at)
            VALUES ('z6aRCrB;k?chQhZy-V%40/O)a]!5IgZUJ§G§)4-zVfSXm%mFEb', '%tJ[;y7u.SU.76at1lbT6W5UFIMkcS[87[%_kLnemi50VXq0lM',
                    'TAA?atDRU[8vt9XnL(wps0W5ET6[y(b.m75Ckm!.pCf;?AVJOz', 'yKji8bVmjG5&t-qEq/xC!]zlARM§F8tj2%0F3wYT!$jp5_]N%b', '2023-07-14 14:42:46.8771');
            """);

    this.jdbcTemplate.execute(
        """
            INSERT INTO secrets(name_fcn_first,
                                name_fcn_second,
                                date_fcn_first,
                                date_fcn_second, created_at)
            VALUES ('Jomj]K.69$e7s.v?qQUu9Gv;lGaZH)vwkvdp/Im&v?;cL6Aj_S', 'oJiGslxcWOP8lfyimq?.G§Zrwp5[pme4T_C!kCSHK§IupMHnhn',
                    'D-SeR[kp0G;3v§z$Q§Lrjs-axobAykeU.gTVU1.kKAGtgodbAw', 'yd_3CbY.SH;vShxlcM)§g%U1bjXoyCaHjOM35FJK8AeB9dFjXh', '2023-09-14 14:42:46.8771')
            """);
  }

  public void writeInitialSecretTwoEntries() {
    this.jdbcTemplate.execute(
        """
            INSERT INTO secrets_two(name_fcn_first,
                                name_fcn_second,
                                date_fcn_first,
                                date_fcn_second, created_at)
            VALUES ('z6aRCrB;k?chQhZy-V%40/O)a]!5IgZUJ§G§)4-zVfSXm%mFEb', '%tJ[;y7u.SU.76at1lbT6W5UFIMkcS[87[%_kLnemi50VXq0lM',
                    'TAA?atDRU[8vt9XnL(wps0W5ET6[y(b.m75Ckm!.pCf;?AVJOz', 'yKji8bVmjG5&t-qEq/xC!]zlARM§F8tj2%0F3wYT!$jp5_]N%b', '2005-06-05 14:42:46.8771');
            """);

    this.jdbcTemplate.execute(
        """
            INSERT INTO secrets_two(name_fcn_first,
                                name_fcn_second,
                                date_fcn_first,
                                date_fcn_second, created_at)
            VALUES ('Jomj]K.69$e7s.v?qQUu9Gv;lGaZH)vwkvdp/Im&v?;cL6Aj_S', 'oJiGslxcWOP8lfyimq?.G§Zrwp5[pme4T_C!kCSHK§IupMHnhn',
                    'D-SeR[kp0G;3v§z$Q§Lrjs-axobAykeU.gTVU1.kKAGtgodbAw', 'yd_3CbY.SH;vShxlcM)§g%U1bjXoyCaHjOM35FJK8AeB9dFjXh', '2015-06-05 15:42:46.8771')
            """);
  }
}
