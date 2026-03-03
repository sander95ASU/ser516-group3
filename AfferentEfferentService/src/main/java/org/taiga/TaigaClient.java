package org.taiga;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// this class handles all the calls to the taiga API
public class TaigaClient {

    private static final String BASE_URL = "https://api.taiga.io/api/v1";

    private final HttpClient client = HttpClient.newHttpClient();

    // logs in and stores the auth token + user id in the login object
    public boolean login(TaigaLoginObject loginObj) throws Exception {

        // taiga wants the body as json, had to look up the format
        String body = "{\"username\": \"" + loginObj.getUsername() +
                "\", \"password\": \"" + loginObj.getPassword() +
                "\", \"type\": \"normal\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // pull the token and user id out of the response so we can use them later
            loginObj.setAuthToken(extractString(response.body(), "auth_token"));
            loginObj.setUserId(extractInt(response.body(), "id"));
            return true;
        }

        System.out.println("Login failed. Status: " + response.statusCode());
        return false;
    }

    public String getProjects(TaigaLoginObject loginObj) throws Exception {
        // filter by member so we only get projects the user is part of
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/projects?member=" + loginObj.getUserId()))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getUserStories(TaigaLoginObject loginObj, int projectId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/userstories?project=" + projectId))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getTasks(TaigaLoginObject loginObj, int projectId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks?project=" + projectId))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // sprints are called milestones in the taiga API for some reason
    public String getSprints(TaigaLoginObject loginObj, int projectId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/milestones?project=" + projectId))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private int extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }
}
