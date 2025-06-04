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

import de.gematik.demis.pseudonymization.Application;
import de.gematik.demis.pseudonymization.core.Endpoint;
import de.gematik.demis.pseudonymization.core.PseudonymizationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfiguration
@AutoConfigureObservability
@Slf4j
public abstract class SpringTestContainerStarter {
  protected static final PostgreSQLContainer<PostgresStarter> postgreSQLContainer =
      PostgresStarter.getInstance();

  private final RestTemplate restTemplate = new RestTemplate();
  @LocalServerPort private int port;

  /**
   * Injects Properties dynamically into the application context.
   *
   * @param registry the registry used by the application context
   */
  @DynamicPropertySource
  protected static void setDynamicProperties(DynamicPropertyRegistry registry) {
    postgreSQLContainer.withReuse(true);
    postgreSQLContainer.start();

    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("secrets.one.generation.init-on-missing", () -> "true");
    registry.add("secrets.two.generation.init-on-missing", () -> "true");
  }

  protected ResponseEntity<PseudonymizationResponse> sendGeneratePseudonymRequest(String body) {
    var entity = new HttpEntity<>(body, DataGenerator.getDefaultHeader(true));

    return restTemplate.exchange(
        createURLWithPort(Endpoint.PSEUDONYMIZATION),
        HttpMethod.POST,
        entity,
        PseudonymizationResponse.class);
  }

  protected void sendGeneratePseudonymRequestWithWrongContentTypeAndAccept(String contentType) {
    HttpEntity<String> entity =
        new HttpEntity<>(
            "",
            DataGenerator.getDefaultHeaderWithContentType(
                contentType, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE));

    restTemplate.exchange(
        createURLWithPort(Endpoint.PSEUDONYMIZATION),
        HttpMethod.POST,
        entity,
        PseudonymizationResponse.class);
  }

  private String createURLWithPort(final String path) {
    return "http://localhost:" + port + path;
  }
}
