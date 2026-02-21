package org.service;

import java.util.List;
import java.util.Scanner;

import org.github.CloneObject;

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
        default:
            System.out.println("Invalid choice. Please select 1 or 2.");
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




}


