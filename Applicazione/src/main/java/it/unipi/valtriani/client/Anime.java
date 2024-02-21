package it.unipi.valtriani.client;

import java.io.Serializable;

/**
 * Creazione di una classe Anime.
 * Rappresenta una riga della tabella Anime nel database
 * @author Lorenzo Valtriani (bobo)
 */
public class Anime implements Serializable {
    public Integer id;
    public String nome;
    public String genere;
    public Integer episodi;
    public String durata;
    public String trailer;
    
    public Anime() {
        
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getGenere() {
        return genere;
    }

    public Integer getEpisodi() {
        return episodi;
    }

    public String getDurata() {
        return durata;
    }

    public String getTrailer() {
        return trailer;
    }
    
    
    
    public Anime(Integer id, String nome, String genere, Integer episodi, String durata, String trailer) {
        this.id = id;
        this.nome = nome;
        this.genere = genere;
        this.episodi = episodi;
        this.durata = durata;
        this.trailer = trailer;
    }
    
    public Anime(String nome, String genere, Integer episodi, String durata, String trailer) {
        this.nome = nome;
        this.genere = genere;
        this.episodi = episodi;
        this.durata = durata;
        this.trailer = trailer;
    }
    
    public Anime(String nome){
        this.nome = nome;
    }

}
