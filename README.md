# WireGuard VPN Bot

A Telegram bot for managing WireGuard VPN configurations. Allows authorized users to create and manage personal VPN device configurations directly through a Telegram chat interface, with admin controls for user management.

---

## Features

- **Self-service VPN config generation** — users generate WireGuard `.conf` files for their devices directly from Telegram
- **Multi-device support** — each user can manage multiple named VPN device configurations
- **Admin panel** — admins can add/block users and manage their devices remotely
- **Persistent state** — conversation state and user data survive bot restarts via RocksDB
- **Actor-per-user model** — each user gets an isolated virtual-thread actor for concurrent, stateful processing
- **WireGuard native integration** — key generation and config sync via `wg` CLI commands

---

## Architecture Overview

```
Telegram Update
      │
      ▼
TelegramService (UpdatesListener)
      │
      ▼
TelegramStateMachine
      │  authorize → TelegramAuthorizationService
      │  getActor  → ActorManagerImpl
      ▼
PrincipalActorImpl (virtual thread per user)
      │  ScopedValue<Context>
      ▼
EventHandlerRegistry → EventHandler (per State)
      │
      ▼
Effect (Message / File / Select / Composite)
      │
      ▼
TelegramIOModule → Telegram API
```

**Key design decisions:**

- **State machine** — each user session is a state machine (MENU → ADD_CONFIG / SELECT_CONFIG → MANAGE_CONFIG). State is persisted to RocksDB so users resume where they left off after a bot restart.
- **Actor model** — `PrincipalActorImpl` wraps a `LinkedBlockingQueue<Job>` processed on a dedicated virtual thread. This eliminates concurrent modification of a user's context without coarse locking.
- **ScopedValue** — `Context.PROVIDER` is a Java 21+ `ScopedValue`, binding a user's full context (principal, state, role) to their virtual thread. Handlers access it via the static `ContextAccessor` without passing context through every method signature.
- **Effect pattern** — handlers return `Effect` objects (sealed interface) rather than performing I/O directly. `TelegramIOModule` translates effects into Telegram API calls, making handlers fully testable.

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 25 |
| Gradle | 9.2+ |
| WireGuard tools (`wg`, `wg-quick`) | any recent |
| Linux | Required (uses `/bin/bash` process execution) |

---

## Configuration

The bot reads properties from files specified via the JVM system property `instance.conf`. Multiple files can be separated by `;`.

### `telegram.dev.properties`

```properties
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_ADMIN_USERS=your_telegram_handle
TELEGRAM_THREADS_COUNT=2
```

### `application.dev.properties`

```properties
VPN_CONFIGURATION_FILE=/etc/wireguard/wg0.conf
VPN_MIN_CLIENT_IP=10.7.0.1
VPN_INTERFACE=wg0
VPN_PORT=51820
VPN_PUBLIC_KEY=<server_public_key>
SERVER_PUBLIC_IP=your.server.ip
DNS_SERVERS=8.8.8.8, 8.8.4.4

DATABASE_PATH=./data
DATABASE_NAME_PRINCIPAL=principals
DATABASE_NAME_PRINCIPAL_STATE=states
DATABASE_NAME_PRINCIPAL_HANDLERS=handlers
```

---

## Running Locally

```bash
# Build
./gradlew build

# Run (IntelliJ run config equivalent)
java \
  -Dinstance.conf=./devops/property/telegram.dev.properties;./devops/property/application.dev.properties \
  -Dlog4j.configurationFile=./devops/logs/log4j2.xml \
  -jar build/libs/WireguardVpnBot-1.0-SNAPSHOT.jar
```

> **Note:** WireGuard key generation (`wg genkey`, `wg pubkey`, `wg genpsk`) and config reload (`wg-quick strip | wg syncconf`) are only executed in production mode. The `wg syncconf` call is gated behind the `PROD` environment variable:
>
> ```bash
> export PROD=true  # enables live wg syncconf calls
> ```

---

## Running in Production

1. Ensure `wg0.conf` exists and WireGuard is running on the server.
2. The bot process needs read/write access to the WireGuard config file and permission to run `wg` commands (typically requires root or `CAP_NET_ADMIN`).
3. Set `PROD=true` in the environment so that `wg syncconf` is called after each peer add/remove.

```bash
PROD=true java \
  -Dinstance.conf=/opt/vpn-bot/telegram.properties;/opt/vpn-bot/application.properties \
  -Dlog4j.configurationFile=/opt/vpn-bot/prod.log4j2.xml \
  -jar WireguardVpnBot-1.0-SNAPSHOT.jar
```

---

## Bot Usage

### User Commands

| Command | Description |
|---------|-------------|
| `/menu` | Open the main menu |
| `Add device` | Generate a new WireGuard config for a device |
| `Manage devices` | View or delete existing device configs |

When adding a device, the bot:
1. Prompts for a unique device name (letters, digits, `_` only)
2. Generates a WireGuard key pair and preshared key
3. Assigns the next available IP in the VPN subnet
4. Appends a `[Peer]` block to the server's `wg0.conf`
5. Sends the client `.conf` file back via Telegram

### Admin Commands

| Command | Description |
|---------|-------------|
| `/manage_users` | Open user management panel |
| `Add new user` | Whitelist a Telegram user by handle |
| `Update user` | Select a user to manage their devices or block them |

Blocking a user removes them from the whitelist, deletes all their VPN peer blocks from `wg0.conf`, and purges their stored data.

---

## Project Structure

```
src/main/java/com/dskroba/vpn/
├── actor/                  # Actor model: Context, PrincipalActorImpl, ActorManagerImpl
├── configuration/          # Spring @Configuration classes (DB, Telegram, VPN)
├── exception/              # Custom exception hierarchy
├── principal/              # Principal (user) domain: entity, repository, service, converters
├── property/               # Spring Boot @ConfigurationProperties records
├── service/                # VpnService (wg config file management), TelegramService
├── statemachine/           # Core state machine: StateMachine, EventHandlerRegistry, effects, events
│   ├── effect/             # Effect sealed interface: Message, File, Select, Composite
│   ├── event/              # Event sealed interface: Message, Command
│   ├── handler/            # AbstractHandler, StateSelector, per-feature handlers
│   │   ├── command/        # MENU, MANAGE_USERS handlers
│   │   ├── users/          # ADD_USER, SELECT_USER, MANAGE_USER handlers
│   │   └── vpn/            # ADD_CONFIG, SELECT_CONFIG, MANAGE_CONFIG handlers
│   └── state/              # State, StateContext, StateFactory, ContextAccessor
├── storage/                # PermanentStorage (RocksDB), KeyValueStorage, ValueConverter
├── telegram/               # TelegramIOModule, TelegramAuthorizationService, TelegramId
├── type/                   # Value types: VpnKeys, UserVpnConfiguration, SelectOption, etc.
└── utils/                  # VpnUtils (IP math, config parsing), ExecUtils, Configuration
```

---

## Known Limitations & TODOs

- **VPN config limit per user** — `checkLimits()` in `AddConfigHandler` is not yet implemented
- **Async persistence listeners** — `listenForPrincipal` and `listenForStateContextChange` in `UpdateListeningService` are synchronous; marked with `TODO: add executor service`
- **`removeUserListener`** in `VpnConfiguration` is a no-op stub
- **File upload events** — `FileUploadEvent` parsing in `TelegramIOModule.parse()` is commented out

---

## License

This project does not currently include a license file.