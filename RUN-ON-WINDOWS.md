# Running on Windows

This guide takes you from a fresh Windows 10/11 machine to a running app.
You only need to install **Java 17 (JDK)**. The project includes a Maven wrapper (`mvnw.cmd`), which downloads Maven on its own, and the database is embedded (H2), so there is no database server to install.

---

## 1. Install Java 17 (JDK)

You need the **JDK** (Java Development Kit), not just the JRE. Any version from 17 up works.

### Option A: installer (easiest)

1. Open https://adoptium.net/temurin/releases/?version=17
2. Choose **Operating System: Windows**, **Architecture: x64**, **Package Type: JDK**, then download the **.msi**.
3. Run the installer. On the "Custom Setup" screen, set both of these to **"Will be installed on local hard drive"**:
   - **Add to PATH**
   - **Set JAVA_HOME variable**
4. Finish the installer.

### Option B: winget (one command)

Open **PowerShell** and run:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

### Check the installation

**Close every open terminal and open a new one** so it picks up the new settings. Then run:

```powershell
java -version
echo $env:JAVA_HOME
```

You should see something like `openjdk version "17.0.x"` and a path such as `C:\Program Files\Eclipse Adoptium\jdk-17...`.
If `JAVA_HOME` prints nothing, set it yourself (see [Troubleshooting](#7-troubleshooting)).

---

## 2. Get the project

### With Git

```powershell
git clone https://github.com/ishitalekhi2007-a11y/java-project.git
cd java-project
```

### Without Git

1. On the GitHub page click **Code → Download ZIP**.
2. Right-click the ZIP and choose **Extract All...**.
3. Open the extracted folder (the one that contains `pom.xml`), click the address bar, type `powershell` and press Enter. A terminal opens in that folder.

> Use a folder path **without special characters** if possible, e.g. `C:\projects\java-project`.

---

## 3. Build (compiles and runs the tests)

In the project folder:

**PowerShell**
```powershell
.\mvnw.cmd clean package
```

**Command Prompt (cmd)**
```bat
mvnw.cmd clean package
```

- The first build downloads Maven and the libraries (about 15 MB), so it needs internet and takes 1–3 minutes. Later builds are fast.
- At the end you should see:
  ```
  Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```
- This creates `target\restaurant-billing.jar`.

To build without running the tests: `.\mvnw.cmd clean package -DskipTests`

---

## 4. Run

```powershell
java -jar target\restaurant-billing.jar
```

You will see:

```
=== Restaurant Billing & Table Management ===

1. Tables   2. Menu   3. Orders   4. Billing   0. Exit
>
```

Type a number and press **Enter**. On the first run the app creates sample data: 6 tables and 10 menu items.
Data is saved in `data\restaurant.mv.db` inside the project folder, so it is still there the next time you start the app.

**Tip:** Windows Terminal (the default on Windows 11) is the nicest place to run it. The classic Command Prompt works too.

---

## 5. Walkthrough: from order to payment

Each line below is what you type, followed by Enter.

| Step | Type | What happens |
|---|---|---|
| See the tables | `1` then `1` | Lists the tables. All are AVAILABLE |
| See the menu | `2` then `1` | Menu grouped by category, with item ids (`#3 Butter Chicken ...`) |
| Seat table 3 | `3`, `1`, `3` | "Opened order #1 for table 3". Table 3 is now OCCUPIED |
| Add 1 Butter Chicken | `3`, `4`, `1` (order id), `3` (item id), `1` (qty) | Order shows the line and the subtotal |
| Add 2 Butter Naan | `3`, `4`, `1`, `6`, `2` | Subtotal 460.00 |
| Remove 1 naan | `3`, `5`, `1`, `6`, `1` | Subtotal 410.00 |
| Undo that removal | `3`, `6`, `1` | "Undid: removed 1 x Butter Naan". Subtotal back to 460.00 |
| Generate the bill with 10% off | `4`, `2`, `1`, `2`, `10`, `y` | Breakdown shown, then "Generated Bill #1 ... total 434.70 UNPAID" |
| Pay by UPI | `4`, `3`, `1`, `3` | "PAID (UPI) – table is now free" |
| Check | `1` then `1` | Table 3 is AVAILABLE again |
| Quit | `0` | Exits |

The bill math for that example: 460.00 − 46.00 (10%) = 414.00, plus 20.70 GST (5%) = **434.70**. There is no service charge because the bill is under 500.

### Menu reference

- **Tables**: list, add, change capacity, delete. An occupied table, or one with past orders, cannot be deleted.
- **Menu**: show, add, edit (press Enter to keep the current name or price), delete. An item that has been ordered cannot be deleted; mark it unavailable instead.
- **Orders**: open an order for a table, list open orders, view an order, add an item, remove an item, **undo last edit** (you can undo several times), cancel an order (this frees the table).
- **Billing**: preview a bill (no changes are saved), generate a bill (closes the order for edits), record a payment (Cash / Card / UPI; this frees the table), list bills.

---

## 6. Useful extras

**Start with a fresh database.** Close the app, then:
```powershell
Remove-Item -Recurse -Force data
```

**Run only the tests:**
```powershell
.\mvnw.cmd test
```

**Open in an IDE:**
- **IntelliJ IDEA**: File → Open, then select the folder that contains `pom.xml`. It is detected as a Maven project. Run `src\main\java\com\restaurant\Main.java`.
- **Eclipse**: File → Import → Maven → Existing Maven Projects, then select the folder. Run `Main.java` as a Java Application.
- **VS Code**: install the "Extension Pack for Java", open the folder, then open `Main.java` and click **Run**.

When you run from an IDE, the input prompts appear in the IDE's Run/Console panel. Click inside it before typing.

---

## 7. Troubleshooting

| Problem | Fix |
|---|---|
| `'java' is not recognized as an internal or external command` | Java is not on PATH. Reinstall with "Add to PATH" enabled, or add `C:\Program Files\Eclipse Adoptium\jdk-17...\bin` to **Path** (see below). Then open a **new** terminal |
| `JAVA_HOME not found in your environment` or `JAVA_HOME is set to an invalid directory` | Set JAVA_HOME (see below). It must point at the JDK folder itself (the one that contains `bin`), **not** at `bin` |
| `The term 'mvnw.cmd' is not recognized` (PowerShell) | PowerShell does not run files from the current folder without a prefix. Type `.\mvnw.cmd` |
| `mvnw.cmd` is not recognized | You are not in the project folder. `cd` into the folder that contains `pom.xml` |
| Build fails while downloading (`Could not transfer artifact`, timeouts) | Check your internet connection. On a college or office network, a proxy may block Maven; try a different network or mobile hotspot for the first build |
| `release version 17 not supported` | Your Java is older than 17. Install JDK 17 and check with `java -version` |
| `Database may be already in use` | The app is already running in another window (H2 allows one instance per data file). Close the other window |
| `Unable to access jarfile target\restaurant-billing.jar` | The build has not run or it failed. Run `.\mvnw.cmd clean package` and look for `BUILD SUCCESS` |

### Setting JAVA_HOME and PATH yourself

1. Press **Win**, type **"environment variables"** and open **Edit the system environment variables** → **Environment Variables...**
2. Under **System variables** (or **User variables**) click **New...**:
   - Variable name: `JAVA_HOME`
   - Variable value: your JDK folder, e.g. `C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot`
3. Select **Path** → **Edit...** → **New** and add `%JAVA_HOME%\bin`.
4. Click OK on every window, **close all terminals**, open a new one, and check with `java -version`.

Or do it in PowerShell for the current window only (replace the path with yours):
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```
