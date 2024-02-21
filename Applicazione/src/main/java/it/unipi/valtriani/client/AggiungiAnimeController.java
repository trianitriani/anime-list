/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static it.unipi.valtriani.client.ScheletroController.lingua;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Classe controller relativa al file aggiungi.fxml
 * @author Lorenzo Valtriani (bobo)
 */
public class AggiungiAnimeController extends ScheletroController {
    @FXML private TableView<Anime> animeRicercaTable = new TableView<>(); // tabella
    @FXML private MenuItem inserisci;   // oggetto del menu a scomparsa per inserire l'anime alla lista
    @FXML private MenuItem vediTrailer; // oggetto del menu a scomparsa per vedere il trailer dell'anime 
    private TabellaAnime ta;            // oggetto che facilita l'interazione con la tabella
    
    @FXML private Text descrizioneAggiungiAnime;
    @FXML private TextField nomeAnime;
    @FXML private Label labelNomeAnime;
    @FXML private Button cercaAnime;
    
    /**
     * Inizializza l'interfaccia.
     * L'obiettivo è creare la tabella anime e eseguire il metodo traduci per tradurre le colonne di questa.
     */
    @FXML 
    @Override
    public void initialize(){
        super.initialize();
        ta = new TabellaAnime(animeRicercaTable); // crea la tabella
        traduci(lingua); 
    }
    
    /**
     * Cerca l'anime che vuole aggiungere alla sua lista.
     * Eseguita quando viene cliccato il bottone per la ricerca degli anime, allora invia una richiesta al server, tramite un Thread Task,
     * La risposta prevede una stringa in formato Json che conterrà gli anime che possono risultare ricercati dall'utente,
     * L'utente ricerca gli anime tramite una parte del loro nome.
     * Nel caso in cui, l'utente per qualche motivo non sia valido mostra un errore a video.
     * Nel caso in cui, la ricerca non va a buon fine, poichè non si trovano anime con quel nome, mostriamo un warning che ne dà notifica all'utente.
     */
    @FXML
    public void cerca(){
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
        
        // controllare che sia stato inserito un qualcosa
        if(nomeAnime.getText().equals("")){
            mostraNotifica("4005"); // devi inserire il nome dell'anime
            return;
        }
        
        // usiamo Task per fare la richiesta al server
        Task cercaAnimeTask;
        cercaAnimeTask = new Task<Void>(){
            @Override
            public Void call(){
                try {
                    // utilizziamo URLEncoder per codificare gli spazi (che possono essere inseriti nel nome) da alcuni '+'
                    URL url = new URL("http://localhost:8080/anime/cerca?nomeAnime="+URLEncoder.encode(nomeAnime.getText(), StandardCharsets.UTF_8));
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    // scelgo di usare una get perchè il parametro sarà sempre uno, l'utente fa la ricerca per nome
                    con.setRequestMethod("GET"); 
                    
                    // invio richiesta e lettura della rispoata
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    
                    // Qua si gestisce la risposta
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    // Otterremo qua un array di Json
                    JsonArray anime = json.getAsJsonArray();
                            
                    // analizzare la risposta del server
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            // rimozione nelle righe nella tabella, per sovrascrirla completamente
                            ta.rimuoviRighe();
                            
                            int i=0;
                            for(; i<anime.size(); i++){
                                JsonObject a = anime.get(i).getAsJsonObject();
                                Integer id = a.get("id").getAsInt();
                                String nome = a.get("nome").getAsString();
                                String genere = a.get("genere").getAsString();

                                // Ottenimento e gestione dei null
                                String episodiProva = a.get("episodi").toString();
                                Integer episodi;
                                if(!episodiProva.equals("null")) episodi = a.get("episodi").getAsInt();
                                else episodi = null;
                                
                                String durata = a.get("durata").getAsString();
                                
                                // Ottenimento e gestione dei null
                                String trailer = a.get("trailer").toString();
                                if(!trailer.equals("null")) trailer = a.get("trailer").getAsString();
                                else trailer = "";
                                
                                // aggiungere righe alla tabella con gli anime
                                Anime aRiga = new Anime(id, nome, genere, episodi, durata, trailer);
                                ta.aggiungi(aRiga); 
                            }
                            
                            semaforoCampi(true);
                            if(i == 0) { // Il risultato della ricerca era vuoto
                                mostraNotifica("2502");
                                animeRicercaTable.setVisible(false);
                            } else animeRicercaTable.setVisible(true);
                        }
                    });
                   
                } catch (IOException ex) {
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            semaforoCampi(true);
                            mostraNotifica("5000");
                        }
                    });                    
                }
            return null;
           }
        };
       semaforoCampi(false);
       new Thread(cercaAnimeTask).start();
    }
    
    /**
     * Inserimento dell'anime selezionato nella lista dell'utente.
     * Aggiungiamo l'anime selezionato alla lista dell'utente, se un anime è stato appunto selezionato.
     * Può capitare, da test che uno riesca ad eseguire da interfaccia grafica il metodo senza selezionare alcun anime, verrà mostrata una notifica in merito.
     * Può capitare che si cerchi di inserire un'anime che si trova già nella lista dell'utente, viee mostrata una notifica. 
     */
    @FXML
    public void inserisci(){
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
        
        // usiamo Task per fare la richiesta al server
        Task aggiungiAnime;
        aggiungiAnime = new Task<Void>(){
            @Override
            public Void call() {
                try {
                    Integer idAnimeSelected = ta.selezionato().id;
                    URL url = new URL("http://localhost:8080/anime?user="+user+"&idAnime="+idAnimeSelected);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    
                    // utilizzo una put poichè l'obiettivo è inserire un elemento nella tabella
                    // uso i parametri invece che il passaggio nel body perchè ciò che mi serve per l'inserimento è solo l'id
                    con.setRequestMethod("PUT");
                    
                    // invio richiesta e lettura della risposta
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    
                    // deserializzazione della risposta del server
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();
                    
                    // analizziamo la risposta
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            // mostrare risultati
                            switch(response){
                                case "2001": // inserimento corretto
                                case "2503": // anime già in lista
                                    mostraNotifica(response);
                                    break;
                                default:
                                    mostraNotifica("5001"); // no implementato
                            }
                        }
                    });  
                } catch (AnimeNonSelezionatoException ex) {
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            mostraNotifica("4006");
                        }
                    });   
                } catch (IOException ex) {
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            mostraNotifica("5000");
                        }
                    });   
                }
                return null;
               }
            };
           new Thread(aggiungiAnime).start();
    } 
    
    /**
     * Apre il browser e mostra il trailer, se è disponibile.
     * Sfrutta il metodo della classe TabellaAnime.
     * Può capitare che l'anime non sia selezionato, che il trailer non sia disponibile, che sia impossibile aprire il trailer, che 
     * l'url del trailer sia errata o che ci sia un problema di connessione, mostriamo le notifiche in modo tale da avvertire l'utente.
     */
    @FXML
    public void vediTrailer() {
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
        try {
            ta.vediTrailer();
        } catch (AnimeNonSelezionatoException ex) {
            mostraNotifica("4006");
        } catch (TrailerNonDisponibileException ex) {
            mostraNotifica("4007");
        } catch (ImpossibileAprireTrailerException ex) {
            mostraNotifica("4008");
        } catch (URISyntaxException | IOException ex) {
            mostraNotifica("5002");
        }
    }
    
    /**
     * Ovverride del metodo traduci.
     * Traduce i testi dei vari oggetti che si trovano in questa classe
     * @param lingua 
     */
    @Override
    protected void traduci(String lingua){
        super.traduci(lingua);
        ta.traduci();
        labelNomeAnime.setText(lang.labelNomeAnime);
        cercaAnime.setText(lang.cercaAnime);
        descrizioneAggiungiAnime.setText(lang.descrizioneAggiungiAnime);
        inserisci.setText(lang.inserisci);
        vediTrailer.setText(lang.vediTrailer);
    }
    
    /**
     * Ovverride del metodo switchTo.
     * Passa ad un altra interfaccia dell'applicazione
     * @param s 
     */
    @Override
    protected void switchTo(Sezioni s) {
        try {
           if(null != s) switch (s) {
                case ListaAnime:
                    App.setRoot("anime");
                    break;
                case Login:
                    App.setRoot("login");
                    break;
                case Registrazione:
                    App.setRoot("registrazione");
                    break;
                case AggiungiAnime:
                    App.setRoot("aggiungi");
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }        
    }
    
    /**
     * Override del metodo nascondiNotifica.
     * La notifica è unica ed è gestita nella classe padre
     */
    @Override
    protected void nascondiNotifica(){
        super.nascondiNotifica();
    }
    
    /**
     * Override del metodo semaforoCampi.
     */
    @Override
    protected void semaforoCampi(boolean accesi){
        super.semaforoCampi(accesi);
        nomeAnime.setDisable(!accesi); 
        cercaAnime.setDisable(!accesi);
    }
}
