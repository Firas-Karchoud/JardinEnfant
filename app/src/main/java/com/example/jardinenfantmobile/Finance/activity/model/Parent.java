package com.example.jardinenfantmobile.Finance.activity.model;

import java.io.Serializable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;

@Entity(
        tableName = "parent",
        foreignKeys = @ForeignKey(
                entity = Enfant.class,
                parentColumns = "id",
                childColumns = "enfant_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Parent implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private String email;

    @ColumnInfo(name = "montant_a_payer")
    private double montantAPayer;

    @ColumnInfo(name = "enfant_id", index = true)
    private int enfantId;

    public Parent(String nom, String prenom, String adresse, String telephone, String email, double montantAPayer) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.montantAPayer = montantAPayer;
        this.enfantId = enfantId;
    }

    public int getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getMontantAPayer() { return montantAPayer; }
    public void setMontantAPayer(double montantAPayer) { this.montantAPayer = montantAPayer; }

    public int getEnfantId() { return enfantId; }

    @Override
    public String toString() {
        return "Parent{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", montantAPayer=" + montantAPayer +
                ", enfantId=" + enfantId +
                '}';
    }
}
