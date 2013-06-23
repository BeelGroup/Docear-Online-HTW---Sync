package org.docear.syncdaemon.config;

import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

import java.util.ArrayList;
import java.util.List;

public class Conf {
    private User user;
    private List<Project> projects;

    public Conf() {
        projects = new ArrayList<Project>();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public String getProjectRootPath(String projectId) {
        for(Project project : projects) {
            if(project.getId().equals(projectId)) {
                return project.getRootPath();
            }
        }
        return null;
    }
}
