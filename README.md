# HexaChess

![Stars](https://img.shields.io/github/stars/Inc44/HexaChess?style=social)
![Forks](https://img.shields.io/github/forks/Inc44/HexaChess?style=social)
![Watchers](https://img.shields.io/github/watchers/Inc44/HexaChess?style=social)
![Repo Size](https://img.shields.io/github/repo-size/Inc44/HexaChess)
![Language Count](https://img.shields.io/github/languages/count/Inc44/HexaChess)
![Top Language](https://img.shields.io/github/languages/top/Inc44/HexaChess)
[![Issues](https://img.shields.io/github/issues/Inc44/HexaChess)](https://github.com/Inc44/HexaChess/issues?q=is%3Aopen+is%3Aissue)
![Last Commit](https://img.shields.io/github/last-commit/Inc44/HexaChess?color=red)
[![Release](https://img.shields.io/github/release/Inc44/HexaChess.svg)](https://github.com/Inc44/HexaChess/releases)
[![Sponsor](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/Inc44)

Gliński's hexagonal chess game implementation in Java.

![HexaChess](HexaChess.png)

## ⚠️ Disclaimers

- **HTTP/HTTPS**: Due to the extreme difficulty of obtaining public CA certificates for local development servers, the HexaChess server currently uses HTTP. Use Cloudflare or similar services to enable an HTTPS proxy (e.g., via the SSL/TLS Full (Strict) encryption option). Otherwise, sensitive data such as passwords, tokens, and other information may be transmitted in plain text over the network and become vulnerable to interception by malicious actors.

## 🚀 Installation

### From Source

```bash
git clone https://github.com/Inc44/HexaChess.git
```

#### Eclipse

```
Open Projects from File System...
Import source: `path/to/HexaChess`
Finish
```

```
Build Path > Configure Build Path... > Libraries > Modulepath
Add Library... > User Library > Next > User Libraries... > New...
User library name: `JavaFX21`
Add External JARs...
path/to/javafx-sdk-21/lib/*.jar
Apply and Close
Finish
Apply and Close
```

```
Run Configurations > Java Application > Main > Arguments
VM arguments: `--module-path "path/to/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media`
```

#### Terminal

Ensure these binaries are in your system's PATH:

- Apache Maven - Version 3.9.11 tested
- GraalVM CE Gluon - Version 23-dev+25.1 tested
- JDK 21 - Version 21.0.9 tested (for Linux)
- Visual Studio Community 2022 - Version 17.14.22 tested (for Windows) (optional, to build)

On Arch Linux:

```bash
sudo pacman -S maven jdk21-openjdk
sudo archlinux-java set java-21-openjdk
export GRAALVM_HOME="path/to/graalvm-java23-gluon"
```

```bash
mvn -v
java --version
$GRAALVM_HOME/bin/native-image --version
```

On Windows:

- Download binary archive and extract from [Apache Maven](https://maven.apache.org/download.cgi)
- Download binary archive and extract from [GraalVM CE Gluon](https://github.com/gluonhq/graal/releases)
- Download and install from [Visual Studio Community 2022](https://visualstudio.microsoft.com/vs/community) (optional, to build)
- Download and install from [WiX Toolset](https://github.com/wixtoolset/wix3/releases) (optional, to produce .msi installer)

##### Visual Studio Installer

Language packs

- English

Individual components

- C++/CLI support for v143 build tools (Latest)
- MSVC v143 - VS 2022 C++ x64/x86 build tools (Latest)
- Windows Universal CRT SDK
- Windows 11 SDK (10.0.26100.7175)

```cmd
setx /M PATH "%PATH%;path\to\apache-maven\bin"
setx /M GRAALVM_HOME "path\to\graalvm-java23-gluon"
```

```cmd
mvn -v
"%GRAALVM_HOME%"\bin\native-image --version
```

```bash
cd HexaChess
mvn -q clean gluonfx:run
```

## 🗄️ Database Setup

On Arch Linux:

```bash
sudo pacman -S apache mariadb php php-apache phpmyadmin
```

### PHP

```bash
sudo vim /etc/php/php.ini
```

```
extension=mysqli
```

### phpMyAdmin

```bash
sudo vim /etc/httpd/conf/extra/phpmyadmin.conf
```

```
Alias /phpmyadmin "/usr/share/webapps/phpMyAdmin"
<Directory "/usr/share/webapps/phpMyAdmin">
    DirectoryIndex index.php
    AllowOverride All
    Options FollowSymlinks
    Require all granted
</Directory>
```

### MariaDB

```bash
sudo systemctl stop mariadb
sudo mariadbd-safe --skip-grant-tables --skip-networking &
```

```bash
mariadb -u root
```

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'password123';
FLUSH PRIVILEGES;
EXIT;
```

```bash
sudo kill $(pgrep mariadb)
sudo systemctl start mariadb
```

### Apache

```bash
sudo vim /etc/httpd/conf/httpd.conf
```

```
#LoadModule mpm_event_module modules/mod_mpm_event.so
LoadModule mpm_prefork_module modules/mod_mpm_prefork.so
LoadModule php_module modules/libphp.so
AddHandler php-script .php
Include conf/extra/php_module.conf
Include conf/extra/phpmyadmin.conf
```

```bash
sudo systemctl restart httpd
```

On Windows:

### XAMPP Control Panel

Start **Apache** and **MySQL**

1. Open `http://localhost/phpmyadmin`
2. Create database `hexachess`
3. Import `Looping/hexachess.sql`

## 🛠️ Build from Source

### Server

#### Linux

```bash
mvn -q clean gluonfx:build -Pserver
```

### Client

#### Linux

```bash
mvn -q clean gluonfx:build
```

#### Windows

Open `x64 Native Tools Command Prompt for VS 2022` instead of regular `cmd`

```cmd
mvn -q clean gluonfx:build
mvn -q gluonfx:package & :: (optional, to produce .msi installer)
```

GluonFX doesn't link version information by default. Therefore, you need to manually link it:

```
rc /fosrc\windows\version.res src\windows\version.rc
cvtres /machine:x64 -out:src\windows\version.obj src\windows\version.res
```

Open "target\gluonfx\log\process-link-*.log"

Replace `IconGroup.obj` with `version.obj`, add quotes to paths containing spaces, and run the modified command

##### Example

```cmd
link target\gluonfx\x86_64-windows\gvm\hexachess\launcher.obj target\gluonfx\x86_64-windows\gvm\tmp\SVM-*\im.bpu.hexachess.main.obj src\windows\version.obj j2pkcs11.lib java.lib net.lib nio.lib prefs.lib zip.lib sunmscapi.lib extnet.lib jvm.lib libchelper.lib advapi32.lib iphlpapi.lib secur32.lib userenv.lib version.lib ws2_32.lib winhttp.lib ncrypt.lib crypt32.lib mswsock.lib /NODEFAULTLIB:libcmt.lib /SUBSYSTEM:WINDOWS /ENTRY:mainCRTStartup comdlg32.lib dwmapi.lib gdi32.lib imm32.lib shell32.lib uiautomationcore.lib urlmon.lib winmm.lib glass.lib javafx_font.lib javafx_iio.lib prism_common.lib prism_d3d.lib /WHOLEARCHIVE:glass.lib /WHOLEARCHIVE:javafx_font.lib /WHOLEARCHIVE:javafx_iio.lib /WHOLEARCHIVE:prism_common.lib /WHOLEARCHIVE:prism_d3d.lib /OUT:target\gluonfx\x86_64-windows\hexachess.exe /LIBPATH:%USERPROFILE%\.gluon\substrate\javafxStaticSdk\21-ea+11.3\windows-x86_64\sdk\lib /LIBPATH:"%GRAALVM_HOME%"\lib\svm\clibraries\windows-amd64 /LIBPATH:"%GRAALVM_HOME%"\lib\static\windows-amd64
```

More details:

- [How to add the version, description and copyright metadata in the generated executable using gluonfx:build?](https://stackoverflow.com/questions/75172666/howto-add-the-version-description-and-copyright-meta-data-in-the-generated-exec)

#### Android

```bash
mvn -q clean gluonfx:build gluonfx:package -Pandroid
```

## 🧾 Configuration

Set environment variable:

```powershell
setx /M DB_URL your_database_url
setx /M DB_USER your_database_user
setx /M DB_PASS your_database_password
setx /M DEV_URL your_dev_api_url
setx /M PROD_URL your_prod_api_url
setx /M PORT your_server_port
setx /M KEY your_server_jwt_key
```

For Linux/macOS:

```bash
echo 'export DB_URL="your_database_url"' >> ~/.bashrc # or ~/.zshrc
echo 'export DB_USER="your_database_user"' >> ~/.bashrc # or ~/.zshrc
echo 'export DB_PASS="your_database_password"' >> ~/.bashrc # or ~/.zsh
echo 'export DEV_URL="your_dev_api_url"' >> ~/.bashrc # or ~/.zshrc
echo 'export PROD_URL="your_prod_api_url"' >> ~/.bashrc # or ~/.z
echo 'export PORT="your_server_port"' >> ~/.bashrc # or ~/.zshrc
echo 'export KEY="your_server_jwt_key"' >> ~/.bashrc # or ~/.zshrc
```

Or create a `.env` file or modify /etc/environment:

```
DB_URL=your_database_url
DB_USER=your_database_user
DB_PASS=your_database_password
DEV_URL=your_dev_api_url
PROD_URL=your_prod_api_url
PORT=your_server_port
KEY=your_server_jwt_key
```

Check by restarting the terminal and using:

```cmd
echo %DB_URL%
echo %DB_USER%
echo %DB_PASS%
echo %DEV_URL%
echo %PROD_URL%
echo %PORT%
echo %KEY%
```

For Linux/macOS:

```bash
echo $DB_URL
echo $DB_USER
echo $DB_PASS
echo $DEV_URL
echo $PROD_URL
echo $PORT
echo $KEY
```

## 📖 Usage Examples

### Server

Open `src/main/java/im/bpu/hexachess/server/Server.java` and run.

### Client

Open `src/main/java/im/bpu/hexachess/Main.java` and run.

## 🐛 Bugs

- The game continues despite checkmate, stalemate, or threefold repetition. Hide the canvas and show appropriate messages.
- The multiplayer black player view is not rotated. Adapt the black view using:
```java
gc.save();
gc.rotate(180);
gc.drawImage(image, -x - offset, -y - offset, size, size);
gc.restore();
```
and
```java
canvas.getTransforms().add(new Rotate(180, canvas.getWidth() / 2, canvas.getHeight() / 2));
```
- Pressed challenge changes the background of the player item.

## ⛔ Known Limitations

- The existing version may be using material not covered during lessons (e.g., enum, AWT, Swing, URI, ImageIO, Thread, etc.).

## 🚧 TODO

- [ ] **Requirements**: Follow the project requirements PDF.
- [ ] **Maven Profiles**: Separate profiles for server and client builds.
- [ ] **Organization**: Separate server and client, packages, and resources.
- [ ] **Style Consistency**: Make sure all styles are in `style.css` (e.g., width, height, etc.).
- [ ] **Android Fonts**: Ensure proper bold font and button icon rendering.
- [ ] **Android Icon**: Fix the smaller icon in the launcher.
- [ ] **Windows Installer**: Fix name, version, and add update functionality.
- [ ] **Player Profile**: Edit profile, change password, upload avatar.
- [ ] **Upload Checks**: Prevent malicious file URLs (e.g., avatar URLs).
- [ ] **Friends List**: Add or remove friends and view online status.
- [ ] **Lobby**: List online players for tournaments, players with challenges, matchmaking, and accepting or declining challenges.
- [ ] **PVP Mode**: Offline player vs. player mode. Allow playing against self via /challenge.
- [ ] **Multiplayer Mode**: Online player vs. player mode.
- [ ] **Timer/Clock**
- [ ] **Database Integration**: Save played games, etc.
- [ ] **Theme Support**: Light/Dark mode.
- [ ] **Sound Effects**: Add audio feedback for moves and game events.
- [ ] **Animations**: Smooth piece movement and captures.
- [ ] **Achievements**
- [ ] **Puzzles**
- [ ] **Tournaments**
- [ ] **Leaderboards**
- [ ] **Help**: Add game rules and multiplayer explanation.
- [ ] **Unit Tests**
- [ ] **UI Test SQL**: Create SQL to test achievements, puzzles, tournaments, and leaderboards UI.
- [ ] **Translations**: Add more languages.
- [ ] **Password Recovery**
- [ ] **Email Verification**
- [ ] **Cache Deletion**
- [ ] **API Caching**
- [ ] **Synthesized/Chiptune Audio**: Replace audio with generated sound.
- [ ] **Raw Config**: Modify to not use `io.github.cdimascio.dotenv.Dotenv`.
- [ ] **Raw Server**: Modify to not use `com.sun.net.httpserver`.
- [ ] **Different Screen Sizes**: Adapt UI for various screen dimensions.
- [ ] **Enemy Movements**: Highlight enemy possible moves (in red).
- [ ] **Game Settings**: Implement configurable game options (e.g., white/black preferred color, aim assist).
- [ ] **Aim Assist**: Highlight better moves with darker color shades.
- [ ] **Improve Performance**: Use multithreading/parallel execution.
- [ ] **LLM Chess Player**: Add a large language model-based chess player for fun :) (and hallucinations (domain expansion, illegal moves, self-capture, etc.)).

## 🙏 Thanks

Creators of:

- [Apache Maven](https://maven.apache.org)
- [Gluon Documentation](https://docs.gluonhq.com)
- [Gluon Samples](https://github.com/gluonhq/gluon-samples)
- [GluonFX plugin for Maven](https://github.com/gluonhq/gluonfx-maven-plugin)
- [GraalVM CE Gluon](https://github.com/gluonhq/graal)
- [JavaFX](https://openjfx.io)

## 🤝 Contribution

Contributions, suggestions, and new ideas are heartily welcomed. If you're considering significant modifications, please initiate an issue for discussion before submitting a pull request.

## 📜 License

[![MIT](https://img.shields.io/badge/License-MIT-lightgrey.svg)](https://opensource.org/licenses/MIT)

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 💖 Support

[![BuyMeACoffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/xamituchido)
[![Ko-Fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/inc44)
[![Patreon](https://img.shields.io/badge/Patreon-F96854?style=for-the-badge&logo=patreon&logoColor=white)](https://www.patreon.com/Inc44)