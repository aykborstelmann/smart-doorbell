# Smart Doorbell
Smart Doorbell for our old house integrated in Google Home

## Documentation
To see more information about how this project is structured and components interact with eachother see [Overview Page](doc/overview.md)

## Setup
### Device
Inside `devices/buzzer` you will find the code for a doorbell buzzer implemented on a ESP8266 (particularly a wemos d1 mini).
First copy `include/example_settings.h` to `include/settings.h` and paste your WiFi SSID, password the doorbell server host and port and your 
newly created doorbell device's id (via API).

Then run `platformio run --target -e d1_mini` to upload the code to your microcontroller (Platformio is required).

### OAuth
This rest application is secured with OAuth2. Particulary it is a resource server in the Auth0 environment. 
For this to work you need to set the following two variables (idealy as environment variables).
* `AUDIENCE` the configured audience for this resource server in Auth0
* `JWT_ISSUER` probably `https://{TENANT}.us.auth0.com/`

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
