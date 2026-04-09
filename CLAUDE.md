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
- **execution/** — `DotEnvRunConfigurationExtension` extends `RunConfigurationExtension`. Injects env vars in `patchCommandLine()` by merging parsed `.env` values into `GeneralCommandLine.environment`. Run Configuration's explicit vars take precedence over `.env` values (only sets if key not already present). Re-reads file on every execution (no caching).

## Plugin Descriptor Split

`plugin.xml` registers platform-level extensions (settings UI, project service). `dotenv-loader-java.xml` registers the `runConfigurationExtension` and is loaded only when `com.intellij.modules.java` is available. This split enables installation on non-Java IDEs (settings work, but injection requires IDE-specific adapters added as future optional dependencies).

## Key Conventions

- Plugin metadata (version, compatibility range) lives in `gradle.properties`, not hardcoded in `build.gradle.kts`
- `kotlin.stdlib.default.dependency=false` — uses the platform-bundled Kotlin stdlib
- Project settings accessed via `DotEnvLoaderSettings.getInstance(project)` companion pattern
