# Configure Exposed with PostgreSQL and HikariCP

This plan outlines the steps to integrate the Exposed ORM into the Ktor project, using a PostgreSQL database and HikariCP for connection pooling.

## Proposed Changes

### Dependencies

#### [MODIFY] [libs.versions.toml](file:///Users/apple/Desktop/Ktor/notes/gradle/libs.versions.toml)
Add versions and library definitions for Exposed, PostgreSQL, and HikariCP.

#### [MODIFY] [build.gradle.kts](file:///Users/apple/Desktop/Ktor/notes/build.gradle.kts)
Add the new dependencies to the `dependencies` block.

### Configuration

#### [MODIFY] [application.yaml](file:///Users/apple/Desktop/Ktor/notes/src/main/resources/application.yaml)
Add database connection properties (URL, driver, user, password).

### Implementation

#### [NEW] [Databases.kt](file:///Users/apple/Desktop/Ktor/notes/src/main/kotlin/Databases.kt)
Create a new file to handle database initialization and HikariCP setup. This will include an extension function `Application.configureDatabases()`.

#### [MODIFY] [application.yaml](file:///Users/apple/Desktop/Ktor/notes/src/main/resources/application.yaml)
Register `com.dreamcode.DatabasesKt.configureDatabases` in the Ktor modules list.

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure dependencies are correctly resolved and the project compiles.
- (Optional) Create a simple test to verify database connectivity if a local PostgreSQL instance is available, or use a mock/test container.

### Manual Verification
- Deploy the application and check the logs for successful database connection initialization.
