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
7. Extend `BaseZenithProvider` so the SDK manages initialization and disposal. If your provider owns resources, override `onDispose()` to release them; resource-free providers need no disposal override.
8. Keep `sdkVersion` in `manifest.json` equal to the SDK contract identifier (`0.1.0`). The SDK and provider are unpublished pre-release software; breaking changes are expected and do not require a major version bump.

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

The canonical SDK source lives in [zenith-provider-sdk](https://github.com/ZenithAnimeORG/zenith-provider-sdk). This template consumes its Maven artifacts; it does not contain a second SDK source tree. CI pins the SDK source commit and publishes it to Maven Local before running Gradle. For a matching local build, check out the same SDK commit:

```bash
git -C ../zenith-provider-sdk checkout 31352b446253ce2aaa657f5cfe5b3f176d66b572
./gradlew -p ../zenith-provider-sdk publishToMavenLocal
```

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

## Trust & Runtime Status

Zenith community packages are intentionally unsigned. Catalog SHA-256 checksums detect transfer/content mismatches; they do not authenticate publishers. The packaging tool's optional signature support is not enforced by Zenith.App and must not be presented as a trust or safety guarantee.

Zenith.App currently fails closed because its native isolated worker runtimes and host capability broker are not yet available. Building a `.zpk` does not mean the app can install or execute it. Do not advertise provider installation as supported until the target platform's sandbox and broker integration has been implemented and verified.

---

## License

MIT License. See [LICENSE](LICENSE) for details.
