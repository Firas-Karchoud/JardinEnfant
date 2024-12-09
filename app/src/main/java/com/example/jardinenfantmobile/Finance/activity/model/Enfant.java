package com.example.jardinenfantmobile.Finance.activity.model;

import java.io.Serializable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "enfant")
public class Enfant implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nom;
    private int age;

    @ColumnInfo(name = "niveau_classe")
    private String niveauClasse;

    @ColumnInfo(name = "parent_id", index = true)
    private int parentId;  // Clé étrangère pour lier à l'entité Parent

    // Constructeur sans 'id'
    public Enfant(String nom, int age, String niveauClasse, int parentId) {
        this.nom = nom;
        this.age = age;
        this.niveauClasse = niveauClasse;
        this.parentId = parentId;
    }

    // Getters et setters
    public int getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getNiveauClasse() { return niveauClasse; }
    public void setNiveauClasse(String niveauClasse) { this.niveauClasse = niveauClasse; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "Enfant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", age=" + age +
                ", niveauClasse='" + niveauClasse + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
