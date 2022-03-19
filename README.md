# Smart Doorbell
Smart Doorbell for our old house integrated in Google Home

## Documentation
To see more information about how this project is structured and components interact with eachother see [Overview Page](doc/overview.md)

## Setup
### Google 

### Add Request Sync and Report State support
The [Request
Sync](https://developers.google.com/assistant/smarthome/develop/request-sync)
feature allows your cloud integration to send a request to Home Graph to
send a new SYNC request. The [Report
State](https://developers.google.com/assistant/smarthome/develop/report-state)
feature allows your cloud integration to proactively provide the current state of
devices to Home Graph without a `QUERY` request.

1. Navigate to the
   [Google Cloud Console API Manager](https://console.developers.google.com/apis)
   for your project ID.
1. Enable the [HomeGraph API](https://console.cloud.google.com/apis/api/homegraph.googleapis.com/overview).
1. Navigate to the [Google Cloud Console API & Services page](https://console.cloud.google.com/apis/credentials).
1. Select **Create Credentials** > **Service account**.
    1. Provide a name for the service account and click **Create and continue**.
    1. Select the role **Service Account Token Creator** and click **Continue**.
    1. Click **Done**.
1. Create a key for the service account key account, and download the JSON file.
    1. Click the pencil icon beside the newly created service account.
    1. Select **Keys** > **Add Key** > **Create new key**.
    1. Create JSON key and save the file as `src/main/resources/smart-doorbell.json`.

### Gradle
#### TLDR
Install gradle and run

```sh
./gradlew build
```

to run build and tests
