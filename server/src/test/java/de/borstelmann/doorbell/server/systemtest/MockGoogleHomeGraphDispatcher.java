package de.borstelmann.doorbell.server.systemtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationResponse;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MockGoogleHomeGraphDispatcher extends Dispatcher {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String DEVICES_REPORT_STATE_AND_NOTIFICATION_PATH = "/v1/devices:reportStateAndNotification";
    public static final String POST_REQUEST = "POST";

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (request.getPath().equals(DEVICES_REPORT_STATE_AND_NOTIFICATION_PATH) && request.getMethod().equals(POST_REQUEST)) {
            ReportStateAndNotificationRequest payload = readPayload(request, ReportStateAndNotificationRequest.class);
            ReportStateAndNotificationResponse response = new ReportStateAndNotificationResponse()
                    .setRequestId(payload.getRequestId());
            return createMockResponse(response);
        }
        return new MockResponse();
    }

    @NotNull
    private static MockResponse createMockResponse(Object responseBody) {
        return new MockResponse()
                .setBody(encodeGzip(responseBody))
                .addHeader("Content-Encoding", "gzip");
    }

    private static <T> T readPayload(RecordedRequest request, Class<T> valueType) {
        try {
            String requestBody = decodeGzipAsString(request.getBody().clone());
            return JacksonFactory.getDefaultInstance().createJsonParser(requestBody).parse(valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Buffer encodeGzip(Object content) {
        Buffer buffer = new Buffer();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                objectMapper.writeValue(gzipOutputStream, content);
            }
            buffer.write(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to zip content", e);
        }

        return buffer;
    }

    static String decodeGzipAsString(Buffer content) {
        try (
                GZIPInputStream gzipInputStream = new GZIPInputStream(content.inputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8))
        ) {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
