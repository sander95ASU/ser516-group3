package org.service;

import java.util.Scanner;
import org.github.githubLoginObject;

public class Main {


    githubLoginObject GLO = new githubLoginObject();
    public int choice = 1;

    public static void main(String[] args) {
        welcomeUser();

    }



public static void welcomeUser(){
    Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Afferent Efferent Service!");
    System.out.println("Please select an option:");
    System.out.println("1. Check from local files");
    System.out.println("2. Check from github");
    int choice = scanner.nextInt();

}




}


