# CLAUDE.md 本文件为 Claude Code (claude.ai/code) 提供代码库指导。

## 项目概述

AndroidComponentDemo 是一个 Android 演示应用，展示 Android 四大组件、多种网络通信库以及 JNI/Native 代码集成。使用 Java 编写，C++ 作为 Native 层，Gradle 构建系统配合版本目录（version catalog）管理依赖。

**包名**：`com.example.demo`

## 构建命令

```bash
./gradlew assembleDebug          # 构建 Debug APK
./gradlew assembleRelease        # 构建 Release APK
./gradlew test                   # 运行单元测试
./gradlew connectedAndroidTest   # 运行设备/模拟器测试
./gradlew clean                  # 清理构建产物
```

> 修改 proto 文件后需要 clean rebuild 以重新生成 stub 代码。

## 架构

单模块应用（`:app`），底部三个导航标签，每个标签内使用 ViewPager2 承载子 Fragment：

### 组件标签页（Component）

通过 `ComponentModuleFragment` 承载 ViewPager2，包含四个子 Fragment，演示 Android 四大组件：

| Fragment | 演示组件 | 核心类 |
|---|---|---|
| ActivityFragment | Activity | `MainActivity`、`SecondActivity`（显式 Intent 跳转） |
| ServiceFragment | Service | `BackgroundService`（后台服务，每5秒打印日志）、`ForegroundService`（前台服务，带常驻通知） |
| BroadcastFragment | BroadcastReceiver | 动态/静态广播注册与发送 |
| ProviderFragment | ContentProvider | `BookProvider` + `BookDatabaseHelper`（SQLite 图书数据 CRUD） |

### 网络标签页（Network）

通过 `NetworkFragment` 承载 ViewPager2，包含三个子 Fragment：

| Fragment | 演示内容 | 核心类 |
|---|---|---|
| HttpFragment | HTTP 请求 | `OkHttpManager`（OkHttp 异步 GET/POST）、`RetrofitManager`（Retrofit + Gson）、`NativeHttpManager`（JNI 调用 libcurl） |
| WebSocketFragment | WebSocket 通信 | `OkHttpWebSocket`（OkHttp WebSocket）、`JavaWebSocket`（Java-WebSocket 库），支持运行时切换实现 |
| GrpcFragment | gRPC 通信 | `GrpcManager`（gRPC 阻塞式 stub，连接 grpcb.in:9001） |

### 数据结构标签页（DataStructure）

`DataStructureFragment`：占位页面，尚未实现。

### 导航与 UI

- 导航定义：`res/navigation/mobile_navigation.xml`
- 底部菜单：`res/menu/bottom_nav_menu.xml`
- 所有 Fragment 使用 ViewBinding（在 build.gradle 中启用 `viewBinding = true`）
- 启用了边缘到边缘（Edge-to-Edge）显示，通过 `WindowInsetsCompat` 适配刘海屏/打孔屏

## Native/JNI 层

CMake 构建 `libdemo.so`，源文件：

- `src/main/cpp/native-lib.cpp` — JNI 示例（`stringFromJNI`），返回 "Hello from C++"
- `src/main/cpp/native-http.cpp` — 基于 libcurl 的 HTTP GET/POST 实现，使用 `__android_log_print` 输出日志（Tag: `NativeHttp`）

链接的预编译静态库（按 ABI 存放在 `src/main/cpp/libcurl/{abi}/`）：

| 库 | 说明 |
|---|---|
| libcurl.a | HTTP 客户端库 |
| libssl.a | OpenSSL SSL/TLS |
| libcrypto.a | OpenSSL 加密 |
| libz.a | zlib 压缩 |

支持的 ABI：`armeabi-v7a`、`arm64-v8a`、`x86_64`（在 build.gradle 的 `ndk.abiFilters` 和 CMakeLists.txt 中配置）

Java 集成：`NativeHttpManager.java` 通过 `System.loadLibrary("demo")` 加载 SO 库，声明 `nativeGetRequest`/`nativePostRequest` 两个 native 方法，在新线程中调用并通过 `OnHttpResponseListener` 回调结果。

## Protobuf/gRPC

- Proto 定义：`src/main/proto/hello.proto`（proto3 语法，package `hello`）
- 定义了 `HelloRequest`/`HelloResponse` 消息和 `HelloService` 服务（含一元/服务端流/客户端流/双向流四种 RPC）
- 生成配置：protobuf-gradle-plugin，protoc 版本 3.25.3，gRPC codegen 版本 1.61.1，均使用 lite 模式
- 生成目录：`build/generated/java/generateDebugProto/java`（消息类）和 `build/generated/java/generateDebugProto/grpc`（gRPC stub），已加入 sourceSets
- `GrpcManager` 使用 `HelloServiceGrpc.newBlockingStub()` 进行阻塞式调用

## 网络管理器设计模式

所有网络管理器均采用单例模式 + 回调监听器模式：

- **OkHttpManager**：OkHttpClient + HttpLoggingInterceptor，异步请求，Handler 切回主线程回调
- **RetrofitManager**：Retrofit + GsonConverter，内部定义 `EchoApiService` 接口（baseUrl: `https://echo.hoppscotch.io`），异步 enqueue
- **NativeHttpManager**：JNI 调用 libcurl，新线程同步请求，回调切主线程
- **GrpcManager**：ManagedChannel + BlockingStub，新线程阻塞调用，Handler 切回主线程
- **OkHttpWebSocket / JavaWebSocket**：均使用 `OnWebSocketListener` 回调，Handler 切主线程

## 关键配置

- **Min SDK**：26 | **Target/Compile SDK**：36 | **Java**：11
- **Gradle**：9.4.1 | **AGP**：9.1.1 | **CMake**：3.22.1
- **版本目录**：`gradle/libs.versions.toml` — 所有依赖版本集中管理
- **构建脚本**：`app/build.gradle`（Groovy DSL，非 KTS）
- **ViewBinding**：已启用
- **ProGuard**：Release 未启用混淆（`minifyEnabled false`）

## 权限

| 权限 | 用途 |
|---|---|
| INTERNET | 网络访问 |
| ACCESS_NETWORK_STATE / ACCESS_WIFI_STATE | 网络状态检测 |
| POST_NOTIFICATIONS | Android 13+ 前台服务通知 |
| FOREGROUND_SERVICE | 前台服务 |
| FOREGROUND_SERVICE_DATA_SYNC | 前台服务类型声明 |
| CALL_PHONE | 拨打电话 |
| RECEIVE_BOOT_COMPLETED | 开机启动广播 |

Android 11+ 包可见性已在 `<queries>` 中声明拨号和浏览器应用的 Intent 查询。

## 目录结构

```
app/src/main/
├── AndroidManifest.xml
├── cpp/
│   ├── CMakeLists.txt
│   ├── native-lib.cpp              # JNI 示例
│   ├── native-http.cpp             # libcurl HTTP 实现
│   └── libcurl/                    # 预编译静态库（按 ABI 分目录）
│       ├── armeabi-v7a/
│       ├── arm64-v8a/
│       └── x86_64/
├── java/com/example/demo/
│   ├── MainActivity.java           # 主界面，底部导航容器
│   ├── SecondActivity.java         # 显式 Intent 跳转目标
│   ├── BackgroundService.java      # 后台服务示例
│   ├── ForegroundService.java      # 前台服务示例
│   ├── BookProvider.java           # ContentProvider 示例
│   ├── BookDatabaseHelper.java     # SQLite 数据库帮助类
│   ├── component/
│   │   ├── ComponentModuleFragment.java  # 组件标签 ViewPager2 容器
│   │   ├── ActivityFragment.java
│   │   ├── ServiceFragment.java
│   │   ├── BroadcastFragment.java
│   │   └── ProviderFragment.java
│   ├── network/
│   │   ├── NetworkFragment.java         # 网络标签 ViewPager2 容器
│   │   ├── http/
│   │   │   ├── HttpFragment.java
│   │   │   ├── OkHttpManager.java
│   │   │   ├── RetrofitManager.java
│   │   │   └── NativeHttpManager.java
│   │   ├── websocket/
│   │   │   ├── WebSocketFragment.java
│   │   │   ├── OkHttpWebSocket.java
│   │   │   └── JavaWebSocket.java
│   │   └── grpc/
│   │       ├── GrpcFragment.java
│   │       └── GrpcManager.java
│   └── datastructure/
│       └── DataStructureFragment.java   # 占位
├── proto/
│   └── hello.proto                # gRPC 服务定义
└── res/
    ├── navigation/mobile_navigation.xml
    ├── menu/bottom_nav_menu.xml
    └── layout/                     # 各 Fragment 和 Activity 的布局文件
```

## 主要依赖

| 库 | 版本 | 用途 |
|---|---|---|
| OkHttp | 5.3.2 | HTTP 客户端 + WebSocket |
| Retrofit | 3.0.0 | REST API 框架 |
| gRPC | 1.81.0 | gRPC 通信框架 |
| Protobuf | 4.34.1 | Protocol Buffers |
| Java-WebSocket | 1.6.0 | WebSocket 客户端 |
| Navigation | 2.9.8 | 导航组件 |
| ViewPager2 | 1.1.0 | 标签页切换 |
| Material | 1.13.0 | Material Design 组件 |
