/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

/**
 * Classe per modellare una notifica all'interfaccia
 * @author Lorenzo Valtriani (bobo)
 */
public class Notifica {
    private int id;             // numero identificativo della notifica
    private String messaggio;   // il testo della notifica
    private Tipo tipo;          // il tipo della notifica     
    
    /**
     * Costruttore della classe Notifica.
     * L'inizializzazione dell'attributo tipo viene fatta un funzione dell'id secondo la regola:
     * Successo: da 2000 a 2499
     * Warning: da 2501 a 2999
     * Errore Client: da 4000 a 4999
     * Errore Server: da 5000 a 5999
     * @param id
     * @param messaggio 
     */
    public Notifica(Integer id, String messaggio) {
        this.id = id;
        this.messaggio = messaggio;
        try {
            if(id >= 2000 && id < 2500){
                tipo = Tipo.Successo;
            } else if(id >= 2501 && id < 3000){
                tipo = Tipo.Warning;
            } else if(id >= 4000 && id < 5000){
                tipo = Tipo.ErroreClient;
            } else if(id >= 5000 && id < 6000){
                tipo = Tipo.ErroreServer;
            } else throw new CodificaNotificaErrataException();
        } catch(CodificaNotificaErrataException e){
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Ovverride della funzione toString
     * @return 
     */
    @Override
    public String toString(){
        return messaggio;
    }

    public Integer getId(){
        return id;
    }
    
    public String getMessaggio(){
        return messaggio;
    }
    
    /**
     * Ritorna il colore che dovrà avere la notifica visualizzata.
     * Se è un errore, sarà rossa, nel caso di uno warning sarà gialla, altrimenti verde.
     * @return colore 
     */
    public String getColore(){
        switch(tipo){
            case ErroreClient:
            case ErroreServer:
                return "red";
            case Successo:
                return "green";
            case Warning:
                return "yellow";
            default:
                return "";
        }
    }
    
}

enum Tipo {
    ErroreClient,
    ErroreServer,
    Successo,
    Warning
};