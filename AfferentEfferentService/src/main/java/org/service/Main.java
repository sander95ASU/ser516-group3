package org.service;

import java.util.Scanner;
import org.github.githubLoginObject;

public class Main {

    Scanner scanner = new Scanner(System.in);
    githubLoginObject GLO = new githubLoginObject();
    public int choice = 1;
    void main(String[] args) {
        welcomeUser();

    }



public void welcomeUser(){
    System.out.println("Welcome to the Afferent Efferent Service!");
    System.out.println("Please select an option:");
    System.out.println("1. Check from local files");
    System.out.println("2. Check from github");
    choice = scanner.nextInt();

}




}


