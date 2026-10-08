# Zenith Provider Template

Official GitHub Template for developing third-party media providers, scrapers, trackers, and subtitle plugins for the [Zenith](https://github.com/ZenithAnimeORG) media player ecosystem.

---

## Getting Started

1. Click **"Use this template"** -> **"Create a new repository"** on GitHub.
2. Clone your repository locally:
   ```bash
   git clone https://github.com/your-username/my-zenith-provider.git
   cd my-zenith-provider
   ```
3. Update `manifest.json`:
   - Set a unique `id` (e.g. `"my_provider"`).
   - Configure `name`, `version`, `author`, and `description`.
   - Declare required capabilities (`CATALOG`, `MEDIA_SOURCE`, `TORRENT_SOURCE`, `SUBTITLES`, `SKIP_TIMINGS`).
   - Declare the network domains your provider accesses in `hosts`.
4. Configure user settings in `settings.json` (optional).
5. Replace `icon.png` with your provider's logo (PNG format).
6. Implement your provider logic in `src/main/kotlin/`.

---

## Project Structure

```
├── manifest.json       # Plugin manifest (metadata, capabilities, network hosts)
├── settings.json       # Declarative UI settings schema rendered by Zenith M3 UI
├── icon.png            # 512x512 PNG icon displayed in the catalog
├── build.gradle.kts    # Gradle build script with assembleZpk task
└── src/
    ├── main/kotlin/    # Provider implementation & factory
    └── test/kotlin/    # Contract verification tests with provider-sdk-testkit
```

---

## Building & Packaging

### Run Unit Tests
```bash
./gradlew test
```

### Build `.zpk` Package
```bash
./gradlew assembleZpk
```
The packaged archive will be generated at:
`build/distributions/my-zenith-provider.zpk`

---

## Signing & Publishing

To sign your `.zpk` with an Ed25519 key for submission to the community repository:
```bash
python3 tools/zpk_tool.py sign build/distributions/my-zenith-provider.zpk --key my_ed25519_key.pem
```

---

## License

MIT License. See [LICENSE](LICENSE) for details.
