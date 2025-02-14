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

## Modes

To cater for various usage scenarios, the connector can be initialised in two modes based on the values of
the `failOnConnectionError` parameter:

- false - if the connector fails to connect on start up, it will silently swallow the exception and set the connector to
  the `inactive` mode.
  This means all queries for storing interactions will be ignored. This is the default behaviour and should be used when
  the main role of the connector is to capture interactions.
- true - if the connector fails to connect on start up, it will throw an exception preventing the startup of the
  application.
  This is a useful mode for applications that rely on the connector for their critical functionality, eg.
  the `lsd-dostributed-generator-ui`.

## Connection

The connector accepts a connection string as the value of the `lsd.dist.connectionString` property from the
`lsd-distributed-connector` library.
For example:

```properties
lsd.dist.connectionString=jdbc:postgresql://localhost:5432/lsd_database?user=sa&password=sa&useUnicode=true&characterEncoding=UTF-8
```

However, if the connection requires a more complicated setup, the connector is able to pick up a `DataSource` bean.
In this case the above property needs to be set to `dataSource`. For example:

```properties
lsd.dist.connectionString=dataSource
spring.datasource.url=jdbc:postgresql://localhost:5432/lsd_database?user=sa&password=sa&useUnicode=true&characterEncoding=UTF-8
```

## Properties

The following properties can be overridden by setting a System property.

| Property Name                              | Default | Description                                                                      |
|--------------------------------------------|---------|----------------------------------------------------------------------------------|
| lsd.dist.db.failOnConnectionError          | false   | See [Modes](#Modes) for details.                                                 |
| lsd.dist.db.traceIdMaxLength               | 32      | To prevent insertion issues the trace id will be trimmed to this length          |
| lsd.dist.db.bodyMaxLength                  | 10000   | To prevent insertion issues the body will be trimmed to this length              |
| lsd.dist.db.requestHeadersMaxLength        | 10000   | To prevent insertion issues the request headers will be trimmed to this length   |
| lsd.dist.db.responseHeadersMaxLength       | 10000   | To prevent insertion issues the response headers will be trimmed to this length  |
| lsd.dist.db.serviceNameMaxLength           | 200     | To prevent insertion issues the service name will be trimmed to this length      |
| lsd.dist.db.targetMaxLength                | 200     | To prevent insertion issues the target name will be trimmed to this length       |
| lsd.dist.db.pathMaxLength                  | 200     | To prevent insertion issues the path will be trimmed to this length              |
| lsd.dist.db.httpStatusMaxLength            | 35      | To prevent insertion issues the HTTP status value will be trimmed to this length |
| lsd.dist.db.httpMethodMaxLength            | 7       | To prevent insertion issues the HTTP method will be trimmed to this length       |
| lsd.dist.db.profileMaxLength               | 20      | To prevent insertion issues the profile name will be trimmed to this length      |
| lsd.dist.db.maxNumberOfInteractionsToQuery | 100     | To prevent query timeouts when dealing with large data sets                      |

NOTE
Make sure that the above `maxLength` values are not set greater than the values in
the [prepareDatabase.sql](src/main/resources/db/prepareDatabase.sql) script.

