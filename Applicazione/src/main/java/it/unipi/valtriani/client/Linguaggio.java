/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.io.Serializable;

/**
 * Classe di riferimento per i testi e per le sue traduzioni
 * @author Lorenzo Valtriani (bobo)
 */
@XStreamAlias("linguaggio")
public class Linguaggio implements Serializable {
    // Generiche 
    public String sottotitolo;
    
    // Scheletro
    public String sezioniPagina;
    public String sezioneAccedi;
    public String sezioneListaAnime;
    public String sezioneAggiungiAnime;
    public String sezioneRegistrazione;
    
    // Login
    public String username;
    public String password;
    public String consiglioLogin;
    
    public String bottoneAccedi;
    
    // Tabella anime
    public String nome;
    public String genere;
    public String episodi;
    public String durata;
    public String trailer;
    public String vediTrailer;
    
    // Lista anime
    public String rimuovi;
    public String descrizioneListaAnime;
    
    // Aggiungi Anime
    public String descrizioneAggiungiAnime;
    public String inserimentoCorretto;
    public String labelNomeAnime;
    public String cercaAnime;
    public String inserisci;
    
    // Registrazione
    public String inventaPassword;
    public String confermaPassword;
    public String bottoneRegistrati;
    public String consiglioRegistrazione;
    
    // Notifiche 2XXX
    public String notificaListaVuota;
    public String notificaRicercaAnimeVuota;
    public String notificaInserimentoCorretto;
    public String notificaAnimeGiaInLista;
    
    // Errori 4XXX
    public String erroreParametriClient;
    public String errorePopolamento;
    public String erroreAccesso;
    public String erroreNonCredenziali;
    public String erroreUsernameInesistente;
    public String erroreNomeAnimeMancanteNellaRicerca;
    public String erroreNessunAnimeSelezionato;
    public String erroreTrailerNonDisponibile;
    public String erroreAperturaTrailerSONonSupportato;
    public String erroreUsernameGiaEsistente;
    public String erroreDigitaCampiRegistrazione;
    public String errorePasswordNonCoincidenti;
    public String erroreAccessoPagina;
    
    // Errori 5XXX
    public String erroreNonImplementato;
    public String erroreServerIrraggiungibile;
    public String erroreGenerico;
}
