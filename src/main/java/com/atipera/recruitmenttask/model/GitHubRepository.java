package com.atipera.recruitmenttask.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepository {

    private String name;
    private Owner owner;
    private ArrayList<Branch> branch;

    public GitHubRepository() {
        branch = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "GitHubRepository{" +
                "name='" + name + '\'' +
                ", owner=" + owner +
                ", branch=" + branch +
                '}';
    }

    public String getName() {
        return name;
    }

    public Owner getOwner() {
        return owner;
    }

    public ArrayList<Branch> getBranch() {
        return branch;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public void setBranch(Branch branch) {
        this.branch.add(branch);
    }
}
