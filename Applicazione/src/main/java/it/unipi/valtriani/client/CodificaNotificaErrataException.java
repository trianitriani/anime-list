/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

/**
 * Eccezione CodificaNotificaErrata
 * @author Lorenzo Valtriani (bobo)
 */
public class CodificaNotificaErrataException extends Exception {

    public CodificaNotificaErrataException() {
        this("La notifica non è stata correttamente codificata");
    }
    
    public CodificaNotificaErrataException(String message) {
        super(message);
    }
    
}
