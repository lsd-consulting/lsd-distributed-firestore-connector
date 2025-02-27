[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-distributed-firestore-connector

![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-firestore-connector)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-firestore-connector)

[![CI](https://github.com/lsd-consulting/lsd-distributed-firestore-connector/actions/workflows/ci.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-firestore-connector/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-distributed-firestore-connector/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-firestore-connector/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-distributed-firestore-connector)](https://github.com/lsd-consulting/lsd-distributed-firestore-connector/releases)
![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-firestore-connector)

## About

This is a Firestore version of the data connector for the distributed data storage.

## Connection

By default, the connector connects to the default Firestore database in Google Cloud Platform, which is named
`(default)`; as a reminder, this is the free-tier eligible Firestore database. This can be overridden by setting the
property `lsd.dist.connectionString`:

```properties
lsd.dist.connectionString=myDatabaseName
```

## GCP Settings

If you have any of the Spring Cloud GCP java dependencies (ex.
`implementation 'com.google.cloud:spring-cloud-gcp-core:x.x.x'`), the `GcpProjectIdProvider` bean will be
autoconfigured when running in a GCP compute environment (ex. GCP Cloud Run). Similarly, the `CredentialsProvider` bean
will be autowired in a GCP compute environment. If you are running into a situation where you need to utilize this
library outside a GCP compute environment, you must provide the project id through the property
`spring.cloud.gcp.project-id` and provide a `CredentialsProvider` bean.

## Properties

The following properties can be overridden by setting a System property.

| Property Name                              | Default | Description                                                                                             |
|--------------------------------------------|---------|---------------------------------------------------------------------------------------------------------|
| lsd.dist.db.maxNumberOfInteractionsToQuery | 100     | To prevent query timeouts when dealing with large data sets                                             |
| lsd.dist.db.timeToLiveDuration             | -1d     | If set to a positive duration, the column will be written with the value `createdAt` plus this duration |

NOTE

The admin repository is not currently implemented. Once more advanced querying capabilities are added to the Firestore
client libraries, it will be implemented.

The TTL column is set to be `expirationAt`. In order to enable the TTL column, either enable it through the GCP console
or via terraform as shown below:

```terraform
resource "google_firestore_field" "expiration_at" {
  project    = "my-project-name"
  database   = "(default)"
  collection = "interceptedInteractions"
  field      = "expirationAt"

  # enables a TTL policy for the document based on the value of entries with this field
  ttl_config {}
}
```
