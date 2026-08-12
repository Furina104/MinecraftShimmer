# MinecraftShimmer ✨

为 Minecraft **1.21.11** 打造的 Fabric 模组，带来一种全新的神奇**「微光流体」（Shimmer Fluid）**！

## 🌟 特性

- **可流动流体**：微光流体能像水一样向四周蔓延、向下流淌，并形成完整水源。
- **无限水源**：微光流体为无限源，可像水一样无限使用。
- **微光桶**：全新物品「微光桶」，可将普通水桶转换为微光桶。
- **掉落分解**：微光流体相关掉落物可分解回原始材料，资源可循环。
- **世界加载**：微光流体随世界一起加载，生成于世界中。

## 📦 环境要求

- **Java 21**（JDK 21）
- **Gradle Wrapper 8.14**
- **Minecraft 1.21.11** + **Fabric Loader 0.16.14** + **Fabric API 0.140.0**

## 🛠️ 项目结构

```
MinecraftShimmer/
├── build.gradle              # Fabric Loom 构建配置（Groovy DSL）
├── settings.gradle           # 项目设置
├── gradle.properties         # 版本与属性配置（MC/Loader/Fabric版本）
├── gradle/wrapper/           # Gradle Wrapper（8.14）
├── gradlew                   # Gradle Wrapper 启动脚本
└── src/
    └── main/
        ├── java/com/example/mymod/
        │   ├── MyMod.java             # 模组主类（入口点）
        │   ├── MyModClient.java       # 客户端入口点
        │   ├── ModRegistries.java     # 物品/流体注册
        │   ├── fluid/                 # 微光流体（WeiguangFluid）
        │   ├── item/                  # 微光桶物品
        │   ├── decompose/             # 掉落物分解
        │   ├── config/                # 配置相关
        │   └── mixin/                 # Mixin 注入类
        └── resources/
            ├── fabric.mod.json        # 模组元数据
            ├── mymod.mixins.json      # Mixin 配置
            └── assets/mymod/          # 资源文件（语言、贴图、模型等）
```

## 🔨 构建

```bash
# 构建模组（生成 jar 到 build/libs/）
./gradlew build

# 清理构建
./gradlew clean
```

## 📥 安装

1. 安装 Fabric Loader 与 Fabric API。
2. 将构建出的 jar 放入 `mods` 文件夹。
3. 启动游戏，体验微光流体的奇妙。

## 📄 许可

MIT License

---

Happy Modding! 🎮