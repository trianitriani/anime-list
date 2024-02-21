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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

/**
 * Classe controller relativa al file anime.fxml
 * @author Lorenzo Valtriani (bobo)
 */
public class AnimeController extends ScheletroController {
    @FXML private TableView<Anime> animeTable = new TableView<>();
    @FXML private MenuItem rimuovi;
    @FXML private MenuItem vediTrailer;
    private TabellaAnime ta;
    
    @FXML private Text descrizioneListaAnime;
    
    @FXML 
    @Override
    public void initialize(){
        super.initialize();
        ta = new TabellaAnime(animeTable);
        traduci(lingua);
                    
        Task inizializzaTabella;
        inizializzaTabella = new Task<Void>(){
            @Override
            public Void call(){
                try {
                    URL url = new URL("http://localhost:8080/anime?user="+user);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    StringBuilder content = new StringBuilder();
                    // try - with - resources (serve a chiudere le risorse nel try in ogni caso)
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
                    
                    Platform.runLater(new Runnable(){
                       @Override
                       public void run(){
                            int i=0;
                            for(; i<anime.size(); i++){
                                JsonObject a = anime.get(i).getAsJsonObject();
                                Integer id = a.get("id").getAsInt();
                                String nome = a.get("nome").getAsString();
                                String genere = a.get("genere").getAsString();

                                String episodiProva = a.get("episodi").toString();
                                Integer episodi;
                                if(!episodiProva.equals("null")) episodi = a.get("episodi").getAsInt();
                                else episodi = null;

                                String durata = a.get("durata").getAsString();
                                
                                // Ottenimento e gestione dei null
                                String trailer = a.get("trailer").toString();
                                if(!trailer.equals("null")) trailer = a.get("trailer").getAsString();
                                else trailer = "";

                                Anime aRiga = new Anime(id, nome, genere, episodi, durata, trailer);
                                ta.aggiungi(aRiga);
                            }
                            
                            if(i == 0) { // La lista dell'utente è vuota
                                mostraNotifica("2501");
                                animeTable.setVisible(false);
                            } else animeTable.setVisible(true);
                        } 
                    });
                
                } catch (IOException ex) {
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            animeTable.setVisible(false);
                            mostraNotifica("5000");
                        }
                    });
                }
                return null;
            }
        };
       new Thread(inizializzaTabella).start();
    }
    
    @FXML
    public void rimuoviAnime() {
        nascondiNotifica();
        Task rimuoviAnime;
        rimuoviAnime = new Task<Void>(){
            @Override
            public Void call(){
                try {
                    // Fare una richiesta delete al server e cancellare i dati nel db
                    Anime animeSelected = ta.selezionato();
                    URL url = new URL("http://localhost:8080/anime?user="+user+"&idAnime="+animeSelected.id);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("DELETE");
                                        
                    // invio richiesta e lettura risposta
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
                    
                    // gestione della risposta
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            switch(response){
                                case "2000":
                                    // rimuovere dalla tabella
                                    ta.rimuovi(animeSelected);
                                    break;
                                default:
                                    // Risposta sconosciuta, quindi non implementata
                                    mostraNotifica("5001");
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
       new Thread(rimuoviAnime).start();
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
     * Ovverride del metodo switchTo.
     * Passa ad un altra interfaccia dell'applicazione
     * @param s
     */
    @Override
    protected void switchTo(Sezioni s) {
        try {
           if(null != s) switch (s) {
                case AggiungiAnime:
                    App.setRoot("aggiungi");
                    break;
                case Login:
                    App.setRoot("login");
                    break;
                case Registrazione:
                    App.setRoot("registrazione");
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
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
        rimuovi.setText(lang.rimuovi);
        vediTrailer.setText(lang.vediTrailer);
        descrizioneListaAnime.setText(lang.descrizioneListaAnime);
    }
}
