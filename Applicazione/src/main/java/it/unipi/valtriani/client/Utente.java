/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

import java.io.Serializable;

/**
 * Creazione di una classe Utente.
 * Rappresenta una riga della tabella Utente nel database
 * @author Lorenzo Valtriani (bobo)
 */
public class Utente implements Serializable {
    public Integer id;
    public String username;
    public String password;
    
    public Utente(){
        
    }
    
    public Utente(String username, String password){
        this.username = username;
        this.password = password;
    }
}
