<div style="text-align:right"><img src="https://raw.githubusercontent.com/gematik/gematik.github.io/master/Gematik_Logo_Flag_With_Background.png" width="250" height="47" alt="gematik GmbH Logo"/> <br/> </div> <br/>

# Release Pseudonymization Service
## Release 3.6.0

- upgraded to spring boot 4

## Release 3.5.1

- arranged jvm options and resource limits
- optimized custom environment variables handling in helm chart
- updated docker base image to gematik1/osadl-alpine-openjdk25-jre:1.0.5

## Release 3.5.0

- Removed istio helm chart
- updated base-image and updated from java 21 to java 25

## Release 3.4.1

- updated Plugins and Libraries

## Release 3.4.0

- Set default rotation period for §7.3 Diseases 'hiv' and 'trp' (Syphilis) to 3652 days (10 years)
- Updated dependencies

## Release 3.3.1

- Updated ospo-resources for adding additional notes and disclaimer
- setting new ressources in helm chart
- setting new timeouts and retries in helm chart
- change base chart to istio hostnames
- update dependencies
- added field name where validation errors occur to log
- added masked view of birthdate input to include a hint what kind of pattern error might have occurred

## Release 3.3.0

- Implement new pseudonymization algorithm
- Update dependencies
- First official GitHub-Release

## Release 3.2.0

- Update dependencies to new spring version

## Release 3.1.0

- Added bloom filter based pseudonym generation

## Release 3.0.0

- Added implementation for §7.3 Pseudonymization
- New Algorithm for the calculation of the Pseudonym

## Release 2.3.0

- Upgraded SpringBoot to 3.3.0

## Release 2.2.0

- Switched storage of Pseudonymization Secret from Redis to Database (PostgreSQL)
- Switched Java version to 21

## Release 2.1.1

- Switched Redis Client from Jedis to Lettuce
- Addressing Timeout issues with Redis
- Updated Helm Charts with Retries

## Release 2.1.0

- Fixed issues with Update of Secrets
- Added Endpoint to support Secret Rotation

## Release 2.0.1

- Upgraded dependencies
- Upgraded Docker Image

## Release 2.0.0

- Rewrite of Service as a SpringBoot Application
- Added Helm Chart for a deployment to Kubernetes
- Added storage of secrets in Redis