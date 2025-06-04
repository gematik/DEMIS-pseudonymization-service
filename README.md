<img align="right" width="250" height="47" src="../media/Gematik_Logo_Flag.png"/> <br/>

# pseudonymization-service

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
       <ul>
        <li><a href="#status">Status</a></li>
        <li><a href="#release-notes">Release Notes</a></li>
        <li><a href="#properties">Properties</a></li>
      </ul>
	</li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#docker-build">Docker build</a></li>
        <li><a href="#docker-run">Docker run</a></li>
        <li><a href="#intellij-cmd">Intellij/CMD</a></li>
        <li><a href="#kubernetes">Kubernetes</a></li>
        <li><a href="#endpoints">Endpoints</a></li>
      </ul>
    </li>
    <li><a href="#security-policy">Security Policy</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About The Project

This Service implements the creation of new Pseudonyms for Notifications based on Bloom filter.

### Quality Gate

[![Quality Gate Status](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=alert_status&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)[![Vulnerabilities](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=vulnerabilities&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)[![Bugs](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=bugs&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)[![Code Smells](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=code_smells&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)[![Lines of Code](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=ncloc&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)[![Coverage](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Apseudonymization-service&metric=coverage&token=0434fb155ba636778fbd3bb69656c364b874cc08)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Apseudonymization-service)

### Release Notes

See [ReleaseNotes](ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started

In order to start the Pseudonymization-Service locally there must be a running Redis instance, which is preconfigured with some secrets.
For convenience reasons there is a `docker-compose.yml` in the root folder of this project that
starts, sets up and imports two secrets to the Redis container, so you can start immediately.

### Prerequisites

* A JDK 21 distribution (e.g. Eclipse Temurin, Amazon Corretto, etc.)
* Maven 3.8+
* Docker (optional)
* A running PostgreSQL Instance, configured with initial Secrets.

### Installation

```sh
mvn clean verify
```

The Project can be built with the following command:

```sh
mvn -e clean verify -DskipTests=true
```

build with docker image:

```docker
docker build -t pseudonymization-service:latest .
```

The Docker Image associated to the service can be built alternatively with the extra profile `docker`:

```docker
mvn -e clean verify -Pdocker
```

The application can be started as Docker container with the following commands:

```shell
docker compose -f docker-compose.yml up -d
docker run --rm --name pseudonymization-service -p 8080:8080 pseudonymization-service:latest
```

## Local
It can be started as SpringBoot Application directly from IntelliJ or running the commands:

```sh
mvn clean verify
java -jar target/pseduonymization-service.jar
```

## Intellij/CMD

Start the spring boot server with: `mvn clean spring-boot:run`
Check the server with: `curl -v localhost:8080/actuator/health`

## Kubernetes

The service can be deployed to Kubernetes by using the available Helm Chart in the repository:

```sh
helm upgrade --install pseudonymization-service deployment/helm/pseudonymization-service/ --namespace MY_NAMESPACE
```

**Important**: PostgreSQL must be deployed and configured separately.

## Usage

Start the spring boot server with: `mvn clean spring-boot:run`
Check the server with: `curl -v localhost:8080/actuator/health`

### Endpoints

| Endpoint                     | Description                                                          |
|------------------------------|----------------------------------------------------------------------|
| `/pseudonymization`          | POST endpoint for creating new Pseudonyms based on input data given. |
| `/actuator/health/`          | Standard endpoint from Actuator.                                     |
| `/actuator/health/liveness`  | Standard endpoint from Actuator.                                     |
| `/actuator/health/readiness` | Standard endpoint from Actuator.                                     |

#### Creation of Pseudonyms

It provides one endpoint `/pseudonymization` for the HTTP POST method, it expects a JSON
as request body, and it is protected by an API key.

Example HTTP request:

[source,httprequest]
----
POST http://localhost:8080/pseudonymization
Content-Type: application/json

{
    "type": "demisPseudonymizationRequest",
    "familyName": "Schmidt",
    "firstName": "Anna",
    "dateOfBirth": "23.02.2012",
    "diseaseCode": "covid19"
}
----

Note:

* All five properties need to be non-empty.
* The property *dateOfBirth* need to be in the format *dd.MM.yyyy*
* Values of the properties *type* are always `"demisPseudonymisationRequest"`.

For legacy compatibility:

* The content type `application/vnd.demis_pseudonymization+json` is supported then the answering
  content type is also `application/vnd.demis_pseudonymization+json`.
* As value for the `type` property `"demisPseudonymizationRequest"` is also allowed.

### Flags

The following flags can be used to configure the generation of common Secrets (§6.1/§7.1/§7.4 Notifications - or secret "one"):

```
secrets.one.generation.enabled=true
secrets.one.generation.init-on-missing=false
secrets.one.generation.days-of-validity=45
secrets.one.generation.secret-length=50
secrets.one.generation.supported-symbols="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.;!?$%&/()[]-_"
# run every day by default at 0:00 - the Generation Schedule should run before the reloading one.
secrets.one.generation.cron-schedule=0 0 0 * * *
# secret reloading configuration
secrets.one.reloading.enabled=true
# run every day by default at 0:10 - the Reloading Schedule should run after the generation one.
secrets.one.reloading.cron-schedule=0 10 0 * * *
```

The following flags can be used to configure the generation of anonymous Secrets (§7.3 Notifications - or secret "two"):

```
secrets.two.generation.enabled=true
secrets.two.generation.init-on-missing=false
secrets.two.generation.days-of-validity=1095
secrets.two.generation.secret-length=50
secrets.two.generation.supported-symbols="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.;!?$%&/()[]-_"
# run every day by default at 0:00 - the Generation Schedule should run before the reloading one.
secrets.two.generation.cron-schedule=0 0 0 * * *
# secret reloading configuration
secrets.two.reloading.enabled=true
# run every day by default at 0:10 - the Reloading Schedule should run after the generation one.
secrets.two.reloading.cron-schedule=0 10 0 * * *
```

| Ops Flag                           | Description                                    |
|------------------------------------|------------------------------------------------|

## Pseudonym Generation

Pseudonym generation uses hash functions, which require secrets. Different
secrets used by the same hash function lead to different pseudonyms for the same
input. This helps protect end-user privacy.

The current implementation uses two hash functions. Each hash function requires
a secret. Pseudonym generation is implemented for names and birthdays. A secret
consists of four passphrases:

- nameFunctionFirst: used for the first hash function
- nameFunctionSecond: used for the second hash function
- dateFunctionFirst: used for the first hash function
- dateFunctionSecond: used for the second hash function

Each secret is salted with the disease code before pseudonym generation. This way it's not possible to identify identical pseudonyms
across different disease. Example:

```
pseudonym_for_name("Foobar", nameFunctionFirst, nameFunctionSecond, "cvpd") = "oewiruw2312"
pseudonym_for_name("Foobar", nameFunctionFirst, nameFunctionSecond, "evdp") = "jhiuhu12uih"
```

Due to legal requirements we have configured two secrets. Which secret is used,
depends on the disease code of a notification. The secrets differ in their
lifespan. The two secrets are:

* "secrets one": common notifications (§6.1/§7.1/§7.4 notifications) have a
  lifespan of weeks
* "secrets two": anonymous notifications (§7.3, e.g. HIV) have a lifespan of
  years

We have chosen to go with these enumarted names, because the lifespan isn't
implied by the disease code and the the configuration for disease codes might
change in the future. It's not practical to have a `short term secret` or
`long term secret`.

### Secret Lifespan

Secrets are grouped into secret pairs. A **secret pair** consists of:

- an outdated secret
- an active secret

To improve end-user privacy a secret is re-generated at the end of it's
lifespan. To allow downstream institutes to analyse long running cases with
multiple notifications we return the pseudonym based on the outdated secret and
the active secret.

This way a chain of pseudonyms is generated. Cases can only be connected if
these pseudonym pairs are stored. Example:

```
form: [outdated, active]

[pseudonymN-3, pseudonymN-2] <- [pseudonymN-2, pseudonymN-1] <- [pseudonymN-1, pseudonymN]
```

### Secret Storage

Each secret is stored in it's own database table.

Look at the `created_at` column to identify active and outdated secret. Active is the one with the largest `created_at` timestamp
and outdated is the one with the second largest `created_at` timestamp.

Application properties to control the regneration and reloading schedule are available.

## Security Policy
If you want to see the security policy, please check our [SECURITY.md](.github/SECURITY.md).

## Contributing
If you want to contribute, please check our [CONTRIBUTING.md](.github/CONTRIBUTING.md).

## License
EUROPEAN UNION PUBLIC LICENCE v. 1.2

EUPL © the European Union 2007, 2016

## Additional Notes and Disclaimer from gematik GmbH

1. Copyright notice: Each published work result is accompanied by an explicit statement of the license conditions for use. These are regularly typical conditions in connection with open source or free software. Programs described/provided/linked here are free software, unless otherwise stated.
2. Permission notice: Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions::
    1. The copyright notice (Item 1) and the permission notice (Item 2) shall be included in all copies or substantial portions of the Software.
    2. The software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.
    3. The software is the result of research and development activities, therefore not necessarily quality assured and without the character of a liable product. For this reason, gematik does not provide any support or other user assistance (unless otherwise stated in individual cases and without justification of a legal obligation). Furthermore, there is no claim to further development and adaptation of the results to a more current state of the art.
3. Gematik may remove published results temporarily or permanently from the place of publication at any time without prior notice or justification.
4. Please note: Parts of this code may have been generated using AI-supported technology.’ Please take this into account, especially when troubleshooting, for security analyses and possible adjustments.

See [LICENSE](LICENSE.md).

## Contact
E-Mail to [DEMIS Entwicklung](mailto:demis-entwicklung@gematik.de?subject=[GitHub]%20pseudonymization-service)
