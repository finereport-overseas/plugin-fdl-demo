# FDL 插件 Demo 工程（独立运行版）

[English](./README_EN.md)

这是一个可**脱离 FDL 源码独立开发、调试和打包**的 FineDataLink（FDL）插件脚手架工程，适合外部开发者与二开场景。

FDL 插件框架基于 FineReport（FR）的插件体系，插件结构与 FR 插件基本一致，核心是 `plugin.xml` 与源码目录。

## 环境要求

| 项 | 要求 |
| --- | --- |
| JDK | 1.8（编译 `sourceCompatibility` / `targetCompatibility` 均为 1.8） |
| Gradle | 使用工程自带的 Gradle Wrapper（7.5.1），无需本地安装 |
| IDE | IntelliJ IDEA（推荐） |
| FDL 版本 | `plugin.xml` 中 `<env-version>` 声明最低支持 4.1.0；本工程依赖坐标为 FDL 5.0 / FR 11.0 |

> 依赖来自帆软私服 `https://mvn.fanruan.com/repository/maven-public/`，请确保网络可访问该仓库。

## 一、工程准备

1. 解压插件工程模板压缩包，用 IDEA 打开（**Open / Import as Gradle Project**）。
2. 调整工程设置：
   - **File → Project Structure**：Project SDK 选择 JDK 1.8，Project language level 选择 8。
   - **Settings → Build, Execution, Deployment → Build Tools → Gradle**：Gradle JVM 选择 JDK 1.8，使用 `gradle-wrapper.properties` 中的 Wrapper 版本。
   - **Settings → Editor → File Encodings**：全部设为 `UTF-8`（插件含中文资源，编码错误会导致乱码）。
3. 修改 `plugin.xml` 中的插件信息：

```xml
<id>com.fr.plugin.dp.demo</id>          <!-- 插件唯一 ID，建议使用自己的包名 -->
<name><![CDATA[fdl插件demo]]></name>     <!-- 插件名 -->
<version>1.0</version>                  <!-- 插件版本 -->
<env-version>4.1.0</env-version>        <!-- 最低支持 FDL 版本 -->
<vendor>author</vendor>                 <!-- 开发者 -->
<description><![CDATA[这是一个fdl插件demo]]></description>
```

4. **刷新 Gradle 配置**（Gradle 面板 → Reload All Gradle Projects）。

刷新成功后即可开始插件开发。

## 二、目录结构

```
plugin-fdl-demo
├── build.gradle                # 主构建脚本（打包、输出路径、依赖版本）
├── pom.xml                     # Maven 侧构建（依赖 FDL 父 POM，二选一使用，日常以 Gradle 为准）
├── plugin.xml                  # 插件描述清单（打包必需）
├── encrypt.xml                 # Ant 脚本，class 加密（防反编译）配置
├── gradle/wrapper/             # Gradle Wrapper（7.5.1）
├── lib/                        # 本地第三方 jar，打包时随插件一起发布
└── src/main
    ├── java/com/fr/plugin/dp/demo
    │   ├── DPApplicationWrapper.java      # 独立调试入口，启动 FDL 应用
    │   ├── PluginConditionalTrigger.java  # 插件生命周期钩子
    │   └── resource/LocaleFinder.java     # 国际化资源定位 + 功能记录点
    └── resources/com/fr/plugin/dp/demo/locale
        ├── locale.properties              # 默认（中文）
        ├── locale_en.properties           # 英文
        ├── locale_zh_CN.properties        # 简体中文
        └── locale_zh_TW.properties        # 繁体中文
```

构建产物说明：

- `build.gradle` 会从 `plugin.xml` 解析 `id` 与 `version`，生成产物名
  `fine-plugin-<id>-<version>.zip`。
- `prepareOutputPath()` 会自动在工程**同级目录**创建
  `env/WEB-INF/plugins/plugin-<id>-<version>/`，并把 `plugin.xml`、`lib/*.jar` 拷入其中；
  编译输出目录（`classes/`）也指向该目录，IDEA 中编译即可热加载。
- 若 `plugin-<id>-` 前缀下存在**其他版本**的目录，构建时会被自动清理，无需手动删除。

## 三、插件开发

### 1. 实现扩展点

在 `src/main/java` 下编写业务实现类，例如一个 FDL 扩展点：

```java
public class FDLPluginHelloWorldProviderImpl extends AbstractFDLPluginHelloWorldProvider {
    @Override
    public String sayHello(String name) {
        return "This is fdl-demo, hello " + name;
    }
}
```

### 2. 在 `plugin.xml` 中注册

```xml
<!-- fdl插件特有的功能扩展 -->
<extra-fdl>
    <FDLPluginHelloWorldProvider class="com.fr.plugin.dp.demo.hello.FDLPluginHelloWorldProviderImpl" />
</extra-fdl>
```

`<extra-fdl>` 中的标签名需与扩展点接口中定义的 `XML_TAG` 常量一致，`class` 为实现类全路径。

其他常用标签：

| 标签 | 说明 |
| --- | --- |
| `<group>fdl</group>` | 标识这是一个 FDL 插件，不要修改 |
| `<conditional-trigger>` | 插件生命周期钩子（启用 / 停用时触发） |
| `<extra-core>` | 主 jar 通用扩展，如国际化 `LocaleFinder` |
| `<extra-fdl>` | FDL 特有扩展点，插件业务能力的主要注册位置 |
| `<function-recorder>` | 功能记录点类，用于埋点统计 |

### 3. 修改后必须做的事（重要）

- **每次调整 `plugin.xml` 之后，都要重新刷新 Gradle 配置**（Reload All Gradle Projects），
  否则构建脚本读取到的仍是旧的 `id` / `version`，产物名与输出目录都会不对。
- 若修改了插件 `id` 或 `version`，`pom.xml` 中的 `plugin.id` / `plugin.version` 属性也要同步修改。
- 新增本地依赖 jar 放到 `lib/` 目录，会被自动打进插件包并拷贝到运行环境。

## 四、代码调试

在 IDEA 中创建一个 **Application** 运行/调试配置：

| 配置项 | 值 |
| --- | --- |
| Main class | `com.fr.plugin.dp.demo.DPApplicationWrapper` |
| Use classpath of module | `plugin-fdl-demo.main` |
| JRE | 1.8 |

直接以 Debug 方式启动即可拉起 FDL 应用，断点可命中插件代码；控制台输出 `plugin start` 表示插件已生效（`plugin stop` 为停用）。

## 五、插件打包

IDEA 中执行 Gradle 任务：`plugin-fdl-demo → Tasks → build → zip`。

或在插件根目录执行命令行：

```bash
# Windows
.\gradlew.bat zip

# Linux / macOS
./gradlew zip
```

打包完成后，在 `build/install/` 下得到插件安装包：

```
build/install/fine-plugin-com.fr.plugin.dp.demo-1.0.zip
```

将该 zip 通过 FDL 的插件管理页面安装即可。

## 六、其他配置说明

- **依赖版本切换**：`build.gradle` 顶部的 `fineVersion`、`fdlVersion`、`fdlThirdVersion`、
  `fdlWebUIVersion`、`fineDatasourceVersion` 可按需修改为对应的开发版本。
- **class 加密**：`build.gradle` 中 `ext.guard` 改为 `true` 后，打包会执行 `encrypt.xml` 的
  `compile_encrypt_javas` 对 class 做防反编译处理（需要 `lib/` 下提供加密工具 jar）。默认为 `false`。
- **资源过滤**：可在 `processResources` 中通过 `include` / `exclude` 控制打进包里的资源文件。
- **国际化**：键值定义在 `src/main/resources/com/fr/plugin/dp/demo/locale/locale*.properties` 中，
  由 `LocaleFinder` 的 `find()` 返回资源基名进行定位。
