package org.docear.syncdaemon.client;

import java.util.List;
import java.util.Map;

/**
 * User: Julius
 * Date: 07.06.13
 * Time: 14:56
 */
public class ListenForUpdatesResponse {
    private Map<String, Long> updatedProjects;
    private Map<String, Long> newProjects;
    private List<String> deletedProjects;

    public Map<String, Long> getUpdatedProjects() {
        return updatedProjects;
    }

    public void setUpdatedProjects(Map<String, Long> updatedProjects) {
        this.updatedProjects = updatedProjects;
    }

    public Map<String, Long> getNewProjects() {
        return newProjects;
    }

    public void setNewProjects(Map<String, Long> newProjects) {
        this.newProjects = newProjects;
    }

    public List<String> getDeletedProjects() {
        return deletedProjects;
    }

    public void setDeletedProjects(List<String> deletedProjects) {
        this.deletedProjects = deletedProjects;
    }
}
