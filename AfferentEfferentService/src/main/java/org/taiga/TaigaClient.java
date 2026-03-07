package org.taiga;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // returns the raw projects list, use listProjects() if you want it parsed
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

    // prints a readable list of the users projects with id, name, and slug
    public void listProjects(TaigaLoginObject loginObj) throws Exception {
        String body = getProjects(loginObj);
        JSONArray projects = new JSONArray(body);

        System.out.println("Your projects:");
        for (int i = 0; i < projects.length(); i++) {
            JSONObject p = projects.getJSONObject(i);
            System.out.println("  ID: " + p.getInt("id") +
                    " | Name: " + p.getString("name") +
                    " | Slug: " + p.getString("slug"));
        }
    }

    public String getProjectById(TaigaLoginObject loginObj, int projectId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/projects/" + projectId))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // look up a project by its slug instead of its id
    public String getProjectBySlug(TaigaLoginObject loginObj, String slug) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/projects/by_slug?slug=" + slug))
                .header("Authorization", "Bearer " + loginObj.getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Could not find project with slug: " + slug + " (status " + response.statusCode() + ")");
            return null;
        }

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

    // gets all the data for a project and structures it so we can see it over time
    // basically a java version of the python get_structure function
    public TaigaProject getStructure(TaigaLoginObject loginObj, int projectId) throws Exception {

        // grab everything we need from the API
        String projectBody = getProjectById(loginObj, projectId);
        String sprintsBody = getSprints(loginObj, projectId);
        String userStoriesBody = getUserStories(loginObj, projectId);
        String tasksBody = getTasks(loginObj, projectId);

        JSONObject projectJson = new JSONObject(projectBody);
        JSONArray sprintsJson = new JSONArray(sprintsBody);
        JSONArray userStoriesJson = new JSONArray(userStoriesBody);
        JSONArray tasksJson = new JSONArray(tasksBody);

        // group tasks by user story id
        Map<Integer, List<TaigaProject.Task>> tasksByStory = new HashMap<>();
        for (int i = 0; i < tasksJson.length(); i++) {
            JSONObject taskJson = tasksJson.getJSONObject(i);

            // user_story can be null if the task isn't assigned to a story
            if (taskJson.isNull("user_story")) continue;

            int usId = taskJson.getInt("user_story");
            if (!tasksByStory.containsKey(usId)) {
                tasksByStory.put(usId, new ArrayList<>());
            }

            TaigaProject.Task task = new TaigaProject.Task();
            task.id = taskJson.getInt("id");
            task.name = taskJson.getString("subject");
            task.createdDate = taskJson.getString("created_date");
            tasksByStory.get(usId).add(task);
        }

        // group user stories by sprint id
        // milestone in taiga = sprint, milestone can also be null for backlog stories
        Map<Integer, List<TaigaProject.UserStory>> storiesBySprint = new HashMap<>();
        for (int i = 0; i < userStoriesJson.length(); i++) {
            JSONObject usJson = userStoriesJson.getJSONObject(i);

            if (usJson.isNull("milestone")) continue;

            int sprintId = usJson.getInt("milestone");
            if (!storiesBySprint.containsKey(sprintId)) {
                storiesBySprint.put(sprintId, new ArrayList<>());
            }

            TaigaProject.UserStory us = new TaigaProject.UserStory();
            us.id = usJson.getInt("id");
            us.name = usJson.getString("subject");
            us.createdDate = usJson.getString("created_date");
            us.tasks = tasksByStory.getOrDefault(us.id, new ArrayList<>());
            storiesBySprint.get(sprintId).add(us);
        }

        // build the final project object
        TaigaProject project = new TaigaProject();
        project.projectName = projectJson.getString("name");
        project.projectId = projectJson.getInt("id");
        project.createdDate = projectJson.getString("created_date");

        // put sprints in, each sprint has its user stories and tasks inside
        for (int i = 0; i < sprintsJson.length(); i++) {
            JSONObject sprintJson = sprintsJson.getJSONObject(i);

            TaigaProject.Sprint sprint = new TaigaProject.Sprint();
            sprint.id = sprintJson.getInt("id");
            sprint.name = sprintJson.getString("name");
            sprint.startDate = sprintJson.optString("estimated_start", "N/A");
            sprint.finishDate = sprintJson.optString("estimated_finish", "N/A");
            sprint.userStories = storiesBySprint.getOrDefault(sprint.id, new ArrayList<>());
            project.sprints.add(sprint);
        }

        return project;
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
