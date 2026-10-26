# FDL Plugin Demo Project (Standalone)

[中文](./README.md)

A FineDataLink (FDL) plugin scaffold that can be **developed, debugged and packaged independently without the FDL source code** — designed for external developers and secondary development.

The FDL plugin framework is built on the FineReport (FR) plugin system, so the plugin structure is basically the same as an FR plugin: the core is `plugin.xml` plus the source directory.

## Requirements

| Item | Requirement |
| --- | --- |
| JDK | 1.8 (`sourceCompatibility` / `targetCompatibility` are both 1.8) |
| Gradle | Use the bundled Gradle Wrapper (7.5.1); no local install needed |
| IDE | IntelliJ IDEA (recommended) |
| FDL version | `<env-version>` in `plugin.xml` declares a minimum of 4.1.0; this project depends on FDL 5.0 / FR 11.0 |

> Dependencies are resolved from the FanRuan repository `https://mvn.fanruan.com/repository/maven-public/`. Make sure it is reachable from your network.

## 1. Project Setup

1. Unzip the plugin project template and open it in IDEA (**Open / Import as Gradle Project**).
2. Adjust the project settings:
   - **File → Project Structure**: set Project SDK to JDK 1.8 and Project language level to 8.
   - **Settings → Build, Execution, Deployment → Build Tools → Gradle**: set Gradle JVM to JDK 1.8 and use the Wrapper version from `gradle-wrapper.properties`.
   - **Settings → Editor → File Encodings**: set everything to `UTF-8` (the plugin contains Chinese resources; a wrong encoding causes garbled text).
3. Edit the plugin information in `plugin.xml`:

```xml
<id>com.fr.plugin.dp.demo</id>          <!-- Unique plugin ID; use your own package name -->
<name><![CDATA[fdl插件demo]]></name>     <!-- Plugin name -->
<version>1.0</version>                  <!-- Plugin version -->
<env-version>4.1.0</env-version>        <!-- Minimum supported FDL version -->
<vendor>author</vendor>                 <!-- Vendor -->
<description><![CDATA[这是一个fdl插件demo]]></description>
```

4. **Refresh the Gradle configuration** (Gradle panel → Reload All Gradle Projects).

Once the refresh succeeds, you can start developing.

## 2. Project Structure

```
plugin-fdl-demo
├── build.gradle                # Main build script (packaging, output path, dependency versions)
├── pom.xml                     # Maven build (depends on the FDL parent POM; use either one, Gradle is the default)
├── plugin.xml                  # Plugin descriptor (required for packaging)
├── encrypt.xml                 # Ant script for class encryption (anti-decompilation)
├── gradle/wrapper/             # Gradle Wrapper (7.5.1)
├── lib/                        # Local third-party jars, shipped together with the plugin
└── src/main
    ├── java/com/fr/plugin/dp/demo
    │   ├── DPApplicationWrapper.java      # Standalone debug entry point, starts the FDL application
    │   ├── PluginConditionalTrigger.java  # Plugin lifecycle hook
    │   └── resource/LocaleFinder.java     # i18n resource locator + function recorder
    └── resources/com/fr/plugin/dp/demo/locale
        ├── locale.properties              # Default (Chinese)
        ├── locale_en.properties           # English
        ├── locale_zh_CN.properties        # Simplified Chinese
        └── locale_zh_TW.properties        # Traditional Chinese
```

Build output notes:

- `build.gradle` parses `id` and `version` from `plugin.xml` and produces
  `fine-plugin-<id>-<version>.zip`.
- `prepareOutputPath()` automatically creates `env/WEB-INF/plugins/plugin-<id>-<version>/`
  **next to the project directory**, and copies `plugin.xml` and `lib/*.jar` into it.
  The compile output directory (`classes/`) also points there, so compiling in IDEA hot-deploys the plugin.
- Directories with the same `plugin-<id>-` prefix but a different version are cleaned automatically.

## 3. Plugin Development

### 1. Implement the extension point

Write your implementation under `src/main/java`, for example an FDL extension point:

```java
public class FDLPluginHelloWorldProviderImpl extends AbstractFDLPluginHelloWorldProvider {
    @Override
    public String sayHello(String name) {
        return "This is fdl-demo, hello " + name;
    }
}
```

### 2. Register it in `plugin.xml`

```xml
<!-- FDL-specific extensions -->
<extra-fdl>
    <FDLPluginHelloWorldProvider class="com.fr.plugin.dp.demo.hello.FDLPluginHelloWorldProviderImpl" />
</extra-fdl>
```

The tag name inside `<extra-fdl>` must match the `XML_TAG` constant defined in the extension interface, and `class` is the fully qualified name of the implementation.

Other common tags:

| Tag | Description |
| --- | --- |
| `<group>fdl</group>` | Marks this as an FDL plugin; do not change it |
| `<conditional-trigger>` | Plugin lifecycle hook (fired when the plugin is enabled / disabled) |
| `<extra-core>` | Core extensions of the main jar, e.g. the internationalization `LocaleFinder` |
| `<extra-fdl>` | FDL-specific extension points — the main place to register plugin capabilities |
| `<function-recorder>` | Function recorder class used for usage metrics |

### 3. After every change (important)

- **After each `plugin.xml` change, refresh the Gradle configuration** (Reload All Gradle Projects).
  Otherwise the build script still reads the old `id` / `version`, and both the artifact name and the output directory will be wrong.
- If you change the plugin `id` or `version`, also update the `plugin.id` / `plugin.version` properties in `pom.xml`.
- Drop any additional local jars into `lib/`; they are packaged automatically and copied to the runtime directory.

## 4. Debugging

Create an **Application** run/debug configuration in IDEA:

| Setting | Value |
| --- | --- |
| Main class | `com.fr.plugin.dp.demo.DPApplicationWrapper` |
| Use classpath of module | `plugin-fdl-demo.main` |
| JRE | 1.8 |

Start it in Debug mode to launch the FDL application with your plugin; breakpoints in plugin code will be hit. `plugin start` in the console means the plugin is active (`plugin stop` means it was disabled).

## 5. Packaging

Run the Gradle task in IDEA: `plugin-fdl-demo → Tasks → build → zip`.

Or from the project root:

```bash
# Windows
.\gradlew.bat zip

# Linux / macOS
./gradlew zip
```

The installable package is generated under `build/install/`:

```
build/install/fine-plugin-com.fr.plugin.dp.demo-1.0.zip
```

Install that zip through the FDL plugin management page.

## 6. Additional Configuration

- **Dependency versions**: edit `fineVersion`, `fdlVersion`, `fdlThirdVersion`, `fdlWebUIVersion`
  and `fineDatasourceVersion` at the top of `build.gradle` to target another development version.
- **Class encryption**: set `ext.guard` in `build.gradle` to `true` and packaging will run
  `compile_encrypt_javas` from `encrypt.xml` to protect classes against decompilation
  (requires the encryption tool jar in `lib/`). Defaults to `false`.
- **Resource filtering**: use `include` / `exclude` inside `processResources` to control which resource files end up in the package.
- **Internationalization**: keys live in `src/main/resources/com/fr/plugin/dp/demo/locale/locale*.properties`;
  the resource base name is returned by `LocaleFinder.find()`.
