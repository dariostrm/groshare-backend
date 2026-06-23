# groshare-backend

This is the backend for the GroShare application, built with [Ktor](https://ktor.io).

## Build & Deploy from Source

Follow these steps to get the application up and running.

<details>
<summary><b>Option 1: Using Docker</b></summary>

### Dependencies
- **Docker & Docker Compose**: [Install Docker](https://docs.docker.com/get-docker/)

### Steps
(Clone the repo if you did not already)
```bash
git clone https://github.com/dariostrm/groshare-backend.git
cd groshare-backend
```

1. **Prepare the network**: The configuration expects an external network named `groshare_network`. Create it by running:
   ```bash
   docker network create groshare_network
   ```
2. **Launch the app**: Run the following command in the root directory:
   ```bash
   docker compose up -d
   ```
The backend will be available on port `9090`. Data is persisted in `~/groshare/data`.
</details>

<details>
<summary><b>Option 2: Local Machine (Gradle)</b></summary>

### Dependencies
- **JDK 21**: [Download from Oracle](https://www.oracle.com/java/technologies/downloads/) or use [SDKMAN!](https://sdkman.io/)
- **Gradle**: Included via `./gradlew` wrapper.

### Steps
1. **Clone the repo**:
   ```bash
   git clone https://github.com/dariostrm/groshare-backend.git
   cd groshare-backend
   ```
2. **Run the application**: Execute the following command:
   ```bash
   ./gradlew run
   ```
The backend will start and listen on the port configured in `src/main/resources/application.yaml` (default is `8080`).

3. **(Optional) Build Fat JAR**: To create a standalone executable:
   ```bash
   ./gradlew buildFatJar
   ```
The JAR will be located in `build/libs/`.
</details>

## Useful Commands

| Task | Command |
|------|---------|
| Build project | `./gradlew build` |
| Run backend locally | `./gradlew run` |
| Build executable JAR | `./gradlew buildFatJar` |
