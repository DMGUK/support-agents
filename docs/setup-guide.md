# Getting Started — Setup Guide

## System Requirements

- Operating system: Windows 10+, macOS 12+, or Ubuntu 20.04+
- Node.js 18+ or Java 17+ depending on your integration type
- Minimum 2 GB RAM available for the agent service
- Outbound HTTPS access on port 443

## Installation Steps

1. Download the latest installer from your account dashboard under **Settings → Downloads**.
2. Run the installer with administrator / sudo privileges.
3. During setup, enter your **License Key** (found under **Account → Billing → License**).
4. Choose an installation directory; the default is `C:\Program Files\SupportPlatform` on Windows or `/opt/support-platform` on Linux/macOS.
5. The installer will automatically register a background service called `sp-agent`.

## Initial Configuration

After installation, open the configuration file located at:
- Windows: `%APPDATA%\SupportPlatform\config.yaml`
- Linux/macOS: `~/.config/support-platform/config.yaml`

Set the following required fields:
```yaml
api_key: "YOUR_API_KEY"
workspace_id: "YOUR_WORKSPACE_ID"
region: "eu-west-1"
log_level: "info"
```

Save the file and restart the service:
- Windows: `sc restart sp-agent`
- Linux/macOS: `sudo systemctl restart sp-agent`

## Verifying the Installation

Run the health-check command:
```bash
sp-cli health
```

Expected output:
```
[OK] Service running
[OK] API key valid
[OK] Workspace connected
[OK] Region: eu-west-1
```

If you see `[FAIL] API key invalid`, double-check the key in config.yaml — trailing whitespace is a common cause.

## Uninstalling

Run `sp-cli uninstall` or use the system's Add/Remove Programs panel. Your data is not deleted automatically — use `sp-cli export` first if you need a backup.