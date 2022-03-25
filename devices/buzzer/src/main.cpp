#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <WebSocketsClient.h>
#include <StompClient.h>
#include <ArduinoJson.h>
#include "settings.h"

const bool useSocketJs = false;
const bool useWss = false;
const char *stompUrl = "/api/v1/websocket";

WebSocketsClient webSocket;

Stomp::StompClient stompClient(webSocket, ws_host, ws_port, stompUrl, useSocketJs);

unsigned long timeStart;
unsigned long timerDuration = 0;

void sendState(bool isOpened) {
    StaticJsonDocument<16> doc;
    doc["isOpened"] = isOpened;
    String output;
    serializeJson(doc, output);
    stompClient.sendMessage("/state", output);
}

void open(unsigned int timer) {
    if (timer != 0) {
        timeStart = millis();
        timerDuration = timer;
    }

    digitalWrite(LED_BUILTIN, LOW);
    sendState(true);
}

void close() {
    timerDuration = 0;

    digitalWrite(LED_BUILTIN, HIGH);
    sendState(false);
}

Stomp::Stomp_Ack_t onOn(Stomp::StompCommand command) {
    Serial.println("On On");

    StaticJsonDocument<32> doc;
    DeserializationError error = deserializeJson(doc, command.body);

    if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
    }

    unsigned int timer = doc["timer"];

    open(timer);
    return Stomp::ACK;
}

Stomp::Stomp_Ack_t onOff(Stomp::StompCommand command) {
    Serial.println("On Off");
    close();
    return Stomp::ACK;
}

void onConnect(Stomp::StompCommand command) {
    Serial.println("Connected, subscribing" + command.body);
    close();
    stompClient.subscribe("/user/commands/open", Stomp::AUTO, onOn);
    stompClient.subscribe("/user/commands/close", Stomp::AUTO, onOff);
}

void onError(Stomp::StompCommand command) {
    Serial.println("ERROR:" + command.body);
}

void connectWiFi() {
    Serial.print("Connecting to WiFi: ");
    Serial.print(wlan_ssid);
    Serial.println(" ...");
    WiFi.mode(WIFI_STA);
    WiFi.begin(wlan_ssid, wlan_password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println(" connected.");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
}

void setup() {
    Serial.begin(115200);
    pinMode(LED_BUILTIN, OUTPUT);
    connectWiFi();

    stompClient.onConnect(onConnect);
    stompClient.onError(onError);
    stompClient.setUser(deviceId);
    stompClient.begin();
}

void timerLoop() {
    if (timerDuration <= 0) {
        return;
    }

    if (millis() - timeStart > timerDuration) {
        close();
    }
}


void loop() {
    stompClient.loop();
    timerLoop();
}