# Spring Boot 連携

streamlit4j は Spring Boot starter を提供しており、WebSocket エンドポイントと同梱の SPA を構成可能なベースパス配下にマウントします。自動構成によって、セッション管理、Spring Security への委譲、Spring Session 統合が明示的な配線なしで処理されます。

## 1. starter を追加する

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

starter は Spring WebMVC / WebSocket への依存をオプションとして宣言しています。アプリケーション側でサーブレットスタックを用意してください。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

## 2. `EntrypointSource` を提供する

streamlit4j はセッションごとに `Runnable` を生成する呼び出し可能オブジェクトを必要とします。この `Runnable` こそが、各ウィジェットイベントで再実行される対象です。

```java
@SpringBootApplication
public class MyApp {

  @Bean
  public EntrypointSource streamlit4jEntrypointSource() {
    return () -> MyDashboard::render;  // factory called once per session
  }

  public static void main(String[] args) {
    SpringApplication.run(MyApp.class, args);
  }
}
```

この Bean を省略した場合、starter は no-op のエントリーポイントへフォールバックします。これはコンテキストロードのスモークテスト用途にしか役立ちません。

## 3. ベースパスを構成する（任意）

```properties
# application.properties
streamlit4j.base-path=/apps/dashboard
```

| パス | マッピング先 |
| --- | --- |
| `${streamlit4j.base-path}/ws` | WebSocket エンドポイント |
| `${streamlit4j.base-path}/**` | 同梱 SPA の静的アセット |

既定のベースパスは `/streamlit4j` です。`/` または空文字に設定すると、Spring の既定ハンドラーを上書きしないよう、静的ハンドラーの登録をスキップします。

## 4. Spring Security による認証

クラスパス上に `spring-security-core` が存在する場合、starter は自動的に `Streamlit4jPrincipalHandshakeInterceptor` を登録します。これはハンドシェイク時に現在の `Authentication` を WebSocket セッション属性へコピーします。

```java
@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/apps/dashboard/**").authenticated()
            .anyRequest().permitAll())
        .oauth2Login(Customizer.withDefaults());
    return http.build();
  }
}
```

`render()` 内では、`Streamlit4jPrincipalHandshakeInterceptor.currentAuthentication(session)` を使って WebSocket セッション属性から認証済み利用者を取得できます。

> **注意**: streamlit4j は利用者に代わって認証を強制することは **ありません**。選択したベースパスに合わせて `SecurityFilterChain` を構成してください。

## 5. セッションのライフサイクルと Spring Session

starter は `Streamlit4jHttpSessionRegistry` を介して streamlit4j 内部のセッションをホスト側の HTTP セッションへ束ね、サーブレットの `HttpSessionListener` を登録します。HTTP セッションが破棄されると（ログアウト、期限切れ、または Spring Session バックエンドによる退避）、束ねられた streamlit4j セッションは自動的に終了します。

これは以下のいずれの場合でも透過的に動作します。

- 通常のサーブレットセッション（既定の Tomcat / Jetty / Undertow）
- Redis、JDBC、MongoDB などをバックエンドとする Spring Session

streamlit4j 側で追加の構成は不要です。プロジェクトに通常どおり `spring-session-*` を追加するだけで構いません。

## 6. エンドツーエンドの例

リポジトリーには `examples/src/main/java/io/streamlit4j/examples/spring/SpringBootHelloApp.java` に最小限のサンプルが同梱されています。

```java
@SpringBootApplication
public class SpringBootHelloApp {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootHelloApp.class, args);
  }

  @Bean
  public EntrypointSource streamlit4jEntrypointSource() {
    return () -> Hello::run;
  }
}
```

ローカルでの実行方法は以下のとおりです。

```sh
mvn -pl examples -am package
java -cp examples/target/streamlit4j-examples-0.1.0-SNAPSHOT.jar:examples/target/dependency/* \
     io.streamlit4j.examples.spring.SpringBootHelloApp
```

（TASK-122 で Spring Boot Maven plugin プロファイルが導入されれば、`mvn -pl examples spring-boot:run` を直接利用できるようになります。）

## 自動構成される内容

| Bean | 用途 | 条件 |
| --- | --- | --- |
| `Streamlit4jApplication` | コンポジションルート（セッション、ダウンロード、レンダラー） | クラスパス上に `Bootstrap` が存在 |
| `Streamlit4jWebSocketHandler` | core ユースケースへの Spring WebSocket アダプター | 常時 |
| `Streamlit4jPrincipalHandshakeInterceptor` | Spring Security の `Authentication` をコピー | クラスパス上に `SecurityContextHolder` が存在 |
| `Streamlit4jHttpSessionRegistry` + `Binder` + `Listener` | HTTP セッションと streamlit4j セッションの紐付け | サーブレット Web 環境 |
| `ResourceRegistration` | 同梱 SPA を `${base-path}/**` で配信 | クラスパス上に `WebMvcConfigurer` が存在 |
| `WebSocketRegistration` | `${base-path}/ws` に WebSocket ハンドラーを登録 | `@EnableWebSocket` が有効 |

## 関連項目

- [Getting Started](./getting-started) — スタンドアロン CLI / Maven ワークフロー
- [Reference](../reference/overview) — 全要素カタログ
