package de.gematik.demis.pseudonymization.config;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ExtendedSecretGenerationConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

  @Test
  void validConfiguration() {
    contextRunner.run(
        context -> {
          SecretTwoGenerationConfiguration config =
              context.getBean(SecretTwoGenerationConfiguration.class);
          assertThat(config.enabled()).isTrue();
          assertThat(config.initOnMissing()).isTrue();
          assertThat(config.daysOfValidity()).isEqualTo(365);
          assertThat(config.secretLength()).isEqualTo(32);
          assertThat(config.supportedSymbols()).isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
          assertThat(config.cronSchedule()).isEqualTo("0 0 12 * * ?");
        });
  }

  @Configuration
  static class TestConfig {
    @Bean
    SecretTwoGenerationConfiguration extendedSecretGenerationConfiguration() {
      return new SecretTwoGenerationConfiguration(
          true, true, 365, 32, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "0 0 12 * * ?");
    }
  }
}
