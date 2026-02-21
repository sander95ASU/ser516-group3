package org.github;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;


public class JGIt {

    //object to get and clone the repo.

String repoUrl;
String branchName;

    public JGIt(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public static void cloneRepository(String repoUrl) throws Exception {

        File localPath = new File("clonedRepo");

        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch("main")
                .call()
                .close();

        System.out.println("Repo cloned.");
    }

    public static void getRepoMetadata(String repoUrl) throws Exception {

        File localPath = new File("tempRepo");

        // Clone repo
        Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .call();

        Repository repository = git.getRepository();
        String branch = repository.findRef("main") != null ? "main" : "master";
        String branchName = repository.findRef("main") != null ? "main" : "master";
        git.checkout().setName(branch).call();
        Iterable<RevCommit> commits = git.log().setMaxCount(1).call();

        for (RevCommit commit : commits) {
            System.out.println("Latest Commit:");
            System.out.println("Message: " + commit.getFullMessage());
            System.out.println("Author: " + commit.getAuthorIdent().getName());
            System.out.println("Commit ID: " + commit.getName());
        }

        git.close();
    }

    public String getRepoUrl() {
        return repoUrl;
    }
}

