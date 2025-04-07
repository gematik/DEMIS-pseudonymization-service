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

import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.COLUMN_ID;
import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.CREATED_AT;
import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.DATE_FUNCTION_FIRST;
import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.DATE_FUNCTION_SECOND;
import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.NAME_FUNCTION_FIRST;
import static de.gematik.demis.pseudonymization.secret.model.SecretEntityDefinitions.NAME_FUNCTION_SECOND;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Abstract class for secret entities.
 *
 * <p>Contains the common fields for all secret entities.
 */
public abstract sealed class AbstractSecretEntity permits SecretTwoEntity, SecretOneEntity {
  @Id
  @Column(value = COLUMN_ID)
  private int id;

  @Column(value = NAME_FUNCTION_FIRST)
  @NotBlank
  private String nameFunctionFirst;

  @Column(value = NAME_FUNCTION_SECOND)
  @NotBlank
  private String nameFunctionSecond;

  @Column(value = DATE_FUNCTION_FIRST)
  @NotBlank
  private String dateFunctionFirst;

  @Column(value = DATE_FUNCTION_SECOND)
  @NotBlank
  private String dateFunctionSecond;

  @Column(value = CREATED_AT)
  @NotNull
  private Timestamp createdTimestamp;

  protected AbstractSecretEntity(
      final int id,
      final String nameFunctionFirst,
      final String nameFunctionSecond,
      final String dateFunctionFirst,
      final String dateFunctionSecond,
      final Timestamp createdTimestamp) {
    this.id = id;
    this.nameFunctionFirst = nameFunctionFirst;
    this.nameFunctionSecond = nameFunctionSecond;
    this.dateFunctionFirst = dateFunctionFirst;
    this.dateFunctionSecond = dateFunctionSecond;
    this.createdTimestamp = createdTimestamp;
  }

  public int getId() {
    return this.id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public @NotBlank String getNameFunctionFirst() {
    return this.nameFunctionFirst;
  }

  public void setNameFunctionFirst(@NotBlank final String nameFunctionFirst) {
    this.nameFunctionFirst = nameFunctionFirst;
  }

  public @NotBlank String getNameFunctionSecond() {
    return this.nameFunctionSecond;
  }

  public void setNameFunctionSecond(@NotBlank final String nameFunctionSecond) {
    this.nameFunctionSecond = nameFunctionSecond;
  }

  public @NotBlank String getDateFunctionFirst() {
    return this.dateFunctionFirst;
  }

  public void setDateFunctionFirst(@NotBlank final String dateFunctionFirst) {
    this.dateFunctionFirst = dateFunctionFirst;
  }

  public @NotBlank String getDateFunctionSecond() {
    return this.dateFunctionSecond;
  }

  public void setDateFunctionSecond(@NotBlank final String dateFunctionSecond) {
    this.dateFunctionSecond = dateFunctionSecond;
  }

  public @NotNull Timestamp getCreatedTimestamp() {
    return this.createdTimestamp;
  }

  public void setCreatedTimestamp(@NotNull final Timestamp createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AbstractSecretEntity that = (AbstractSecretEntity) o;
    return this.id == that.id
        && Objects.equals(this.nameFunctionFirst, that.nameFunctionFirst)
        && Objects.equals(this.nameFunctionSecond, that.nameFunctionSecond)
        && Objects.equals(this.dateFunctionFirst, that.dateFunctionFirst)
        && Objects.equals(this.dateFunctionSecond, that.dateFunctionSecond)
        && Objects.equals(this.createdTimestamp, that.createdTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.id,
        this.nameFunctionFirst,
        this.nameFunctionSecond,
        this.dateFunctionFirst,
        this.dateFunctionSecond,
        this.createdTimestamp);
  }

  @Override
  public String toString() {
    return "AbstractSecretEntity{"
        + "id="
        + this.id
        + ", nameFunctionFirst='"
        + this.nameFunctionFirst
        + '\''
        + ", nameFunctionSecond='"
        + this.nameFunctionSecond
        + '\''
        + ", dateFunctionFirst='"
        + this.dateFunctionFirst
        + '\''
        + ", dateFunctionSecond='"
        + this.dateFunctionSecond
        + '\''
        + ", createdTimestamp="
        + this.createdTimestamp
        + '}';
  }
}
