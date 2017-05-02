package org.hibernate.test;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CONTACT")
public class Contact {

    private ContactTypeEnum type;

    private Integer id;

    private String nom;

    private String prenom;

    private String nomComplementaire;

    public Contact() {
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", unique = true, nullable = false, updatable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 16)
    public ContactTypeEnum getType() {
        return type;
    }

    public void setType(final ContactTypeEnum type) {
        this.type = type;
    }

    @Column(name = "NOM", nullable = false, length = 128)
    public String getNom() {
        return nom;
    }

    public void setNom(final String nom) {
        this.nom = nom;
    }

    @Column(name = "PRENOM", length = 128)
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(final String prenom) {
        this.prenom = prenom;
    }

    @Column(name = "NOM_COMPLEMENTAIRE", length = 128)
    public String getNomComplementaire() {
        return nomComplementaire;
    }

    public void setNomComplementaire(final String nomComplementaire) {
        this.nomComplementaire = nomComplementaire;
    }
}
