/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unipi.valtriani.client;

import static it.unipi.valtriani.client.ScheletroController.lang;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Classe per semplificare la creazione della tabella anime, che viene usata su due interfacce diverse.
 * @author Lorenzo Valtriani (bobo)
 */
public class TabellaAnime {
    @FXML private final TableView<Anime> animeTabella;
    @FXML private final TableColumn nomeCol;
    @FXML private final TableColumn genereCol;
    @FXML private final TableColumn episodiCol;
    @FXML private final TableColumn durataCol;
    private final ObservableList<Anime> ol;         // interfaccia che implementa List   
    
    /**
     * Creazione della tabella anime.
     * Viene passato il riferimento dell'oggetto TableView
     * @param at 
     */
    public TabellaAnime(TableView at){
        animeTabella = at;
        
        // Dichiariamo le colonne e collghiamole ai dati delle istanza della classe Anime che mostreremo
        nomeCol = new TableColumn();
        nomeCol.setMinWidth(349);
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        
        genereCol = new TableColumn();
        genereCol.setMinWidth(85);
        genereCol.setCellValueFactory(new PropertyValueFactory<>("genere"));

        episodiCol = new TableColumn();
        episodiCol.setMinWidth(85);
        episodiCol.setCellValueFactory(new PropertyValueFactory<>("episodi"));

        durataCol = new TableColumn();
        durataCol.setMinWidth(100);
        durataCol.setCellValueFactory(new PropertyValueFactory<>("durata"));
        
        // Colleghiamo gli elementi TableColumn appena dichiarati alla tabella
        animeTabella.getColumns().addAll(nomeCol, genereCol, episodiCol, durataCol);
        // Creazione di un array osservabile
        ol = FXCollections.observableArrayList();
        // Colleghiamo l'array alla tabella, così che se vengono messi o tolti dalla tabella, anche questa verrà modificata.
        animeTabella.setItems(ol);
    }
    
    /**
     * Tradurre i testi delle colonne della tabella
     */
    public void traduci(){
        nomeCol.setText(lang.nome);
        genereCol.setText(lang.genere);
        episodiCol.setText(lang.episodi);
        durataCol.setText(lang.durata);
    }
    
    /**
     * Aggiungere un anime alla tabella
     * @param a 
     */
    public void aggiungi(Anime a) {
        ol.add(a);
    }
    
    /**
     * Rimuovere un anime dalla tabella
     * @param a 
     */
    public void rimuovi(Anime a){
        ol.remove(a);
    }
    
    /**
     * Rimuovere tutti gli anime dalla tabella
     */
    public void rimuoviRighe(){
        ol.removeAll(ol);
    }
    
    /**
     * Ritorna l'anime selezionato nella tabella.
     * @return Anime
     * @throws AnimeNonSelezionatoException quando non è stato selezionato alcun anime
     */
    public Anime selezionato() throws AnimeNonSelezionatoException {
        Anime a = animeTabella.getSelectionModel().getSelectedItem();
        if(a == null) throw new AnimeNonSelezionatoException();
        return a;
    }
    
    /**
     * Metodo che implementa l'apertura di un browser all'utente e che mostri direttamente il trailer dell'anime.
     * @throws AnimeNonSelezionatoException quando non è stato selezionato alcun anime
     * @throws TrailerNonDisponibileException quando nel database non è disponibile il trailer 
     * @throws ImpossibileAprireTrailerException quando il SO dell'utente non si riconosce
     * @throws URISyntaxException quando la sintassi dell'url del trailer è sbagliata
     * @throws IOException quando accade un errore di connessione
     */
    public void vediTrailer() throws AnimeNonSelezionatoException, TrailerNonDisponibileException, ImpossibileAprireTrailerException, URISyntaxException, IOException {
        Anime animeSelected = selezionato();
        String url = animeSelected.getTrailer();
        // Se l'url è vuota allora non è disponibile
        if(url.equals("")) throw new TrailerNonDisponibileException();
        
        String myOS = System.getProperty("os.name").toLowerCase();
        // Controllare se è supportata la classe nel SO del client
        if(Desktop.isDesktopSupported()) { // Windows
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url)); // apre il browser
        } else { 
            // Utilizziamo i comandi da terminale per aprire il link
            Runtime runtime = Runtime.getRuntime();
            if(myOS.contains("mac")) { // Apples
                runtime.exec("open " + url);
            } else if(myOS.contains("nix") || myOS.contains("nux")) { // Linux flavours 
                runtime.exec("xdg-open " + url);
            } else throw new ImpossibileAprireTrailerException();  
        }
    }
}
