package org.service;

import java.util.List;
import java.util.Scanner;

import org.github.CloneObject;
import org.taiga.DeliveryMetrics;
import org.taiga.TaigaClient;
import org.taiga.TaigaLoginObject;

public class Main {

    int Afferent;
    int Efferent;


    //githubLoginObject GLO = new githubLoginObject();
    //public int choice = 1;

    public static void main(String[] args) {
        welcomeUser();

    }



public static void welcomeUser(){
    Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Afferent Efferent Service!");
    System.out.println("Please select an option:");
    System.out.println("1. Check from local files (coming soon)");
    System.out.println("2. Check from github");
    System.out.println("3. Connect to Taiga");
    int choice = scanner.nextInt();

    switch (choice) {
        case 1:
            System.out.println("Please use option 2");
            break;
        case 2:
            System.out.println("Please enter the GitHub repository URL:");
            String repoUrl = scanner.next();
            goClone(repoUrl);
            break;
        case 3:
            goTaiga(scanner);
            break;
        default:
            System.out.println("Invalid choice. Please select 1, 2, or 3.");
    }

}

public static void goClone(String s){
    CloneObject jgit = new CloneObject(s);
    String a = jgit.getRepoUrl();
    if(a.equals(s) && s.contains("github.com/")){
        try {
            CloneObject.getRepoMetadata(s);
            String localPath = CloneObject.cloneRepository(s);

            List<Metrics> results =
                    Analyzer.analyze(localPath);


            results.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Error cloning repository: " + e.getMessage());
        }
    } else {
        System.out.println("Invalid repo URL");
    }

}

public static void goTaiga(Scanner scanner) {
    System.out.println("Enter your Taiga username:");
    String username = scanner.next();
    System.out.println("Enter your Taiga password:");
    String password = scanner.next();

    TaigaClient taiga = new TaigaClient();
    TaigaLoginObject loginObj = new TaigaLoginObject(username, password);

    try {
        boolean loggedIn = taiga.login(loginObj);
        if (!loggedIn) {
            System.out.println("Could not log in to Taiga.");
            return;
        }

        // if we get here login worked
        System.out.println("Logged in! Fetching your projects...");
        String projects = taiga.getProjects(loginObj);
        System.out.println("Projects: " + projects);

        System.out.println("Enter a project ID to get data for:");
        int projectId = scanner.nextInt();

        System.out.println("What data do you want?");
        System.out.println("1. User Stories");
        System.out.println("2. Tasks");
        System.out.println("3. Sprints");
        int dataChoice = scanner.nextInt();

        String data;
        switch (dataChoice) {
            case 1:
                data = taiga.getUserStories(loginObj, projectId);
                break;
            case 2:
                data = taiga.getTasks(loginObj, projectId);
                break;
            case 3:
                data = taiga.getSprints(loginObj, projectId);
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        System.out.println(data);

        System.out.println("Analyzing delivery metrics for the project...");
        try {
            List<DeliveryMetrics> metrics = taiga.getDeliveryMetrics(loginObj, projectId);
            for (DeliveryMetrics m : metrics) {
                System.out.println(m);
            }
        } catch (Exception e) {
            System.out.println("Error analyzing delivery: " + e.getMessage());
        }

    } catch (Exception e) {
        System.out.println("Error connecting to Taiga: " + e.getMessage());
    }
}




}
