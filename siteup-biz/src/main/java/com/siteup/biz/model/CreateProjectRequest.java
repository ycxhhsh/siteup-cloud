package com.siteup.biz.model;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name; // Only project name needed, userId comes from token

    // Explicit getters and setters for compilation
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
