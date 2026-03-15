# HubSpot Integration Guide

## Overview

The HubSpot integration allows you to sync contacts, deals, and events bidirectionally between the Support Platform and HubSpot CRM.

## Prerequisites

- A HubSpot account with **Super Admin** or **App Marketplace** permissions
- Support Platform Pro plan or higher
- Support Platform agent installed and running

## Step 1 — Create a HubSpot Private App

1. In HubSpot, go to **Settings → Integrations → Private Apps**.
2. Click **Create a private app**.
3. Name it (e.g. "Support Platform Sync").
4. Under **Scopes**, enable:
   - `crm.objects.contacts.read`
   - `crm.objects.contacts.write`
   - `crm.objects.deals.read`
   - `timeline`
5. Click **Create app** and copy the generated **Access Token**.

## Step 2 — Configure the Integration

In your Support Platform dashboard go to **Integrations → HubSpot** and enter:
- **HubSpot Access Token**: the token from Step 1
- **Sync Direction**: Bidirectional (recommended)
- **Contact Match Field**: Email (default) or External ID
- **Sync Frequency**: Real-time (webhook) or Every 15 minutes

Save and click **Test Connection**. You should see a green **Connected** badge.

## Step 3 — Field Mapping

Navigate to **Integrations → HubSpot → Field Mapping** to map HubSpot properties to Support Platform fields.

| HubSpot Property | Support Platform Field |
|------------------|----------------------|
| email            | user.email           |
| firstname        | user.first_name      |
| lastname         | user.last_name       |
| company          | user.organization    |
| hs_lead_status   | user.crm_status      |

## Common Integration Errors

### 401 Unauthorized

The HubSpot token has expired or been revoked.

**Fix:** Regenerate the token in HubSpot under **Settings → Private Apps**, then update it in **Support Platform → Integrations → HubSpot → Edit**.

### Contacts not syncing

**Cause 1:** The contact's email address is missing in HubSpot.
**Fix:** Ensure all HubSpot contacts have a valid email.

**Cause 2:** Sync is paused due to rate limiting.
**Fix:** Check **Integrations → HubSpot → Sync Log** for `RATE_LIMITED` entries. The integration retries automatically.

### Duplicate contacts created

**Cause:** Contact Match Field is set to External ID but records are missing that field.
**Fix:** Switch the match field to Email or populate the External ID field in HubSpot first.

### Deal data not appearing

**Cause:** The `crm.objects.deals.read` scope was omitted when creating the Private App.
**Fix:** Edit the Private App in HubSpot, add the missing scope, and click **Update app**.

## Disabling the Integration

Go to **Integrations → HubSpot → Edit → Disable**. Existing synced data is retained.