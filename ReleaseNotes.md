<img align="right" width="250" height="47" src="Gematik_Logo_Flag.png"/> <br/>    

# Release Pseudonymization Service

## Release 3.4.0
### changed
- Set default rotation period for §7.3 Diseases 'hiv' and 'trp' (Syphilis) to 3652 days (10 years)
- Updated dependencies

## Release 3.3.1
### changed
- Updated ospo-resources for adding additional notes and disclaimer
- setting new ressources in helm chart
- setting new timeouts and retries in helm chart
- change base chart to istio hostnames
- update dependencies
- added field name where validation errors occur to log
- added masked view of birthdate input to include a hint what kind of pattern error might have occurred

## Release 3.3.0
### changed
- Implement new pseudonymization algorithm
- Update dependencies
- First official GitHub-Release

## Release 3.2.0
### changed
- Update dependencies to new spring version

## Release 3.1.0
### changed
- Added bloom filter based pseudonym generation

## Release 3.0.0
### changed
- Added implementation for §7.3 Pseudonymization
- New Algorithm for the calculation of the Pseudonym

## Release 2.3.0
### changed
- Upgraded SpringBoot to 3.3.0

## Release 2.2.0

### changed
- Switched storage of Pseudonymization Secret from Redis to Database (PostgreSQL)
- Switched Java version to 21

## Release 2.1.1

### changed
- Switched Redis Client from Jedis to Lettuce
- Addressing Timeout issues with Redis
- Updated Helm Charts with Retries

## Release 2.1.0

### changed
- Fixed issues with Update of Secrets
- Added Endpoint to support Secret Rotation

## Release 2.0.1

### changed
- Upgraded dependencies
- Upgraded Docker Image

## Release 2.0.0

### changed
- Rewrite of Service as a SpringBoot Application
- Added Helm Chart for a deployment to Kubernetes
- Added storage of secrets in Redis
