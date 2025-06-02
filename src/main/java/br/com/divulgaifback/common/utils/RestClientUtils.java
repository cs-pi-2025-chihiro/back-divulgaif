package br.com.divulgaifback.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class RestClientUtils {

    private final String server;
    private final HttpClient client;
    private final List<String> headers;

    public RestClientUtils(String server) {
        client = HttpClient.newHttpClient();
        this.server = server;
        headers = new ArrayList<>();
    }

    public RestClientUtils(String server, List<String> headers) {
        client = HttpClient.newHttpClient();
        this.server = server;
        this.headers = headers;
    }

    public HttpResponse<String> get(String uri) throws IOException, InterruptedException {
        return get(uri, headers);
    }

    public HttpResponse<String> post(String uri, String json) throws IOException, InterruptedException {
        return post(uri, json, headers);
    }

    public HttpResponse<String> put(String uri, String json) throws IOException, InterruptedException {
        return put(uri, json, headers);
    }

    public HttpResponse<String> delete(String uri) throws IOException, InterruptedException {
        return delete(uri, headers);
    }

    public HttpResponse<String> patch(String uri, String json) throws IOException, InterruptedException {
        return patch(uri, json, headers);
    }

    public HttpResponse<String> get(String uri, List<String> headers) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(server + uri))
                .timeout(Duration.of(20, ChronoUnit.SECONDS)).headers(headers.toArray(String[]::new))
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String uri, String json, List<String> headers)
            throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(server + uri))
                .POST(BodyPublishers.ofString(json)).timeout(Duration.of(20, ChronoUnit.SECONDS))
                .version(Version.HTTP_1_1).headers(headers.toArray(String[]::new))
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    public HttpResponse<String> put(String uri, String json, List<String> headers)
            throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(server + uri))
                .PUT(BodyPublishers.ofString(json)).timeout(Duration.of(20, ChronoUnit.SECONDS))
                .headers(headers.toArray(String[]::new)).build();
        return client.send(request, BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String uri, List<String> headers) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(server + uri)).DELETE()
                .timeout(Duration.of(20, ChronoUnit.SECONDS)).headers(headers.toArray(String[]::new))
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    public HttpResponse<String> patch(String uri, String json, List<String> headers)
            throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(server + uri))
                .method("PATCH", BodyPublishers.ofString(json)).timeout(Duration.of(20, ChronoUnit.SECONDS))
                .headers(headers.toArray(String[]::new)).build();
        return client.send(request, BodyHandlers.ofString());
    }
}

