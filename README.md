# SER516 Group 3

Team Members:

-Andrew

-Sarah

-Lily

-Mohamed

###Project Overview:

Group 3 has been tasked with creating services that in sum take a chunk of code and show the number of afferent and efferent couplings on the default branch.

Requirements: Default branch must either be named 'main' or 'master' and is case sensitive. The repository must be public on Github.com. You must know the github repository URL.

##Scope of services:

This will take a repository online, clone the repository onto a local machine storage location, iterate through the file system (ignoring nested classes) and keep track of each files Afferent and Efferent Couplings. This will not run VIA Docker, but will be run directly through Gradle. The results will be outputted to the console, there is no support for Grafana.

##Pre-requisites

-Docker installation

-Java 22

-A local or git based codebase and repository

-Grafana

##To use our services

###Running via Gradle directly

Load the code base, or access it via your Terminal/CMD/Powershell. First run a 'gradle clean', a 'gradle build', and then a 'gradle run' command. This will execute the service.

It will ask you to select a local or github repository, the only choice is Github, selecting local will ask you to choose github.
Enter the URL of the repository you want to analyze.
3 The service will run, and output the results, on a per file basis, in the console.
The program then terminates, but can be run indefinetly using step 1. It automatically overwrites previous checks and files every run.
