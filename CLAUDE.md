# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliJ Platform plugin (Kotlin) that injects environment variables from `.env` files into Run Configurations at execution time. Targets IntelliJ IDEA 2024.2–2026.1 (unified IntelliJ IDEA target). Uses optional dependency on `com.intellij.modules.java` for `RunConfigurationExtension`; settings UI is platform-level (available on all JetBrains IDEs).

## Build Commands

```bash
# Compile and package
./gradlew build

# Run unit tests (JUnit 4)
./gradlew test

# Launch sandboxed IDE for manual testing
./gradlew runIde

# Verify plugin compatibility
./gradlew verifyPlugin
```

Requires JDK 21. Build config is driven by `gradle.properties` (platformVersion, sinceBuild/untilBuild, etc.). Uses IntelliJ Platform Gradle Plugin 2.x (`org.jetbrains.intellij.platform`).

## Architecture

The plugin has three layers connected through `DotEnvLoaderSettings` (project service):

- **parser/** — `DotEnvParser` is a pure Kotlin utility (no IntelliJ dependencies) that parses `.env` files. Supports double/single quoted values, multiline, escape sequences, `export` prefix, inline comments. Independently testable.
- **settings/** — `DotEnvLoaderSettings` (`SimplePersistentStateComponent<BaseState>`) stores `enabled` flag and `envFilePath` per project in `.idea/dotenv-loader.xml`. `DotEnvLoaderConfigurable` (`BoundConfigurable`) provides the Settings > Tools > Dotenv Loader UI.
- **execution/** — Two injection mechanisms for different Run Configuration types:
  - `DotEnvRunConfigurationExtension` (`RunConfigurationExtension`): For Java-based Run Configurations. Injects env vars in `patchCommandLine()` by merging `.env` values into `GeneralCommandLine.environment`. Run Configuration's explicit vars take precedence (only sets if key not already present). Re-reads file on every execution (no caching).
  - `DotEnvExecutionListener` (`ExecutionListener`): For Gradle/Maven/Python/Go/Node.js. Injects in `processStartScheduled()` and restores in `processStarted()` / `processNotStarted()` / `processTerminated()`. Uses strategy pattern: `ExternalSystemInjector` (for ExternalSystem-based configs like Gradle/Maven) and `ReflectionInjector` (reflection-based for generic run profiles).
  - `DotEnvLoader`: Shared utility for reading settings and parsing `.env` files.

## Plugin Descriptor Split

`plugin.xml` registers platform-level extensions (settings UI, project service, `DotEnvExecutionListener`). `dotenv-loader-java.xml` registers `DotEnvRunConfigurationExtension` and is loaded only when `com.intellij.modules.java` is available. On non-Java IDEs, `DotEnvExecutionListener` handles Gradle/Maven and generic run profiles via the strategy pattern.

## Key Conventions

- Plugin metadata (version, compatibility range) lives in `gradle.properties`, not hardcoded in `build.gradle.kts`
- `kotlin.stdlib.default.dependency=false` — uses the platform-bundled Kotlin stdlib
- Project settings accessed via `DotEnvLoaderSettings.getInstance(project)` companion pattern
