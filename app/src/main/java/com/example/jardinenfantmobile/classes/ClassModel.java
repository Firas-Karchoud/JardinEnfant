package com.example.jardinenfantmobile.classes;

import java.util.List;

public class ClassModel {
    private String id;
    private String name;
    private String description;
    private List<String> studentIds;

    // Constructeurs
    public ClassModel() {
        // Firebase nécessite un constructeur vide
    }

    public ClassModel(String id, String name, String description, List<String> studentIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.studentIds = studentIds;
    }

    public ClassModel(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }
}

