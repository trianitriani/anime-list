package it.unipi.valtriani.client;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Classe controller padre di tutte le altri classi controller.
 * L'utilità è avere una maggior modularità nel codice, qua vengono gestite cose generiche a tutte (o magari quasi) le classi che la estendono.
 * Per esempio il menu in alto, oppure metodi come traduci o switchTo
 * @author Lorenzo Valtriani (bobo)
 */
public class ScheletroController {    
    // menu lingue
    @FXML protected Menu menuLingue;
    @FXML protected MenuItem italiano;
    @FXML protected MenuItem inglese;
    
    // menu sezioni
    @FXML protected Menu menuPagine;
    @FXML protected MenuItem paginaLogin;
    @FXML protected MenuItem paginaListaAnime;
    @FXML protected MenuItem paginaAggiungiAnime;
    @FXML protected MenuItem paginaRegistrazione;
    
    // notifica, testo per tutti errori/notifiche
    @FXML protected Text notifica;
    
    // variabili statiche che si condividono le varie instanze di classi controller
    protected static boolean popolato = false;
    protected static String lingua = "Italiano";
    protected static String user = "";
    protected static Linguaggio lang;
    
    private final Map<String, Notifica> codNotify = new HashMap<String, Notifica>();
    // ultima notifica mostrata, server per la traduzione di questa
    private Notifica lastNotify;     
    
    // Elenco delle sezioni dell'app
    protected enum Sezioni {
                            Login,
                            ListaAnime,
                            AggiungiAnime,
                            Registrazione
                          };
    
    
    /**
     * Inizializzazione della interfaccia.
     * Crea i menu, l'oggetto lang per la traduzione dei testi
     */
    @FXML 
    protected void initialize() {
        nascondiNotifica();
        
        // per la traduzione dei testi
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias("linguaggio", Linguaggio.class);
        lang = (Linguaggio)xstream.fromXML(getClass().getResource("languages/linguaggio"+lingua+".xml"));
        
        inserisciCodificheNotifiche();
        
        // Creiamo le immagini delle bandiere che affiancano le lingue nel menu lingue
        ImageView italianFlag = new ImageView();
        ImageView englishFlag = new ImageView();
        italianFlag.setImage(new Image(getClass().getResource("images/flags/italiano.png").toExternalForm()));
        italianFlag.setFitWidth(25);
        italianFlag.setFitHeight(20);
        englishFlag.setImage(new Image(getClass().getResource("images/flags/inglese.png").toExternalForm()));
        englishFlag.setFitHeight(20);
        englishFlag.setFitWidth(25);
        
        // Inserimento dinamico del menu lingue
        italiano = new MenuItem("Italiano", italianFlag);
        menuLingue.getItems().add(italiano);
        inglese = new MenuItem("English", englishFlag);
        menuLingue.getItems().add(inglese);
        
        // vengono settati degli eventi che vengono chiamati quando si cliccano
        italiano.setOnAction(e -> traduci("Italiano"));
        inglese.setOnAction(e -> traduci("English"));
        
        // Inserimento dinamico del menu pagine
        paginaLogin = new MenuItem();
        menuPagine.getItems().add(paginaLogin);
        paginaListaAnime = new MenuItem();
        menuPagine.getItems().add(paginaListaAnime);
        paginaAggiungiAnime = new MenuItem();
        menuPagine.getItems().add(paginaAggiungiAnime);
        paginaRegistrazione = new MenuItem();
        menuPagine.getItems().add(paginaRegistrazione);
        
        // vengono settati degli eventi che vengono chiamati quando si cliccano
        paginaLogin.setOnAction(e -> switchTo(Sezioni.Login));
        paginaListaAnime.setOnAction(e -> switchTo(Sezioni.ListaAnime));
        paginaAggiungiAnime.setOnAction(e -> switchTo(Sezioni.AggiungiAnime));
        paginaRegistrazione.setOnAction(e -> switchTo(Sezioni.Registrazione));
    }
    
    /**
     * Metodo per la traduzione dei testi.
     * @param lingua 
     */
    protected void traduci(String lingua) {
        ScheletroController.lingua = lingua;
        menuLingue.setText(lingua);
        
        // viene ridefinita poichè la lingua deve essere cambiata
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias("linguaggio", Linguaggio.class);
        lang = (Linguaggio)xstream.fromXML(getClass().getResource("languages/linguaggio"+lingua+".xml"));
        
        menuPagine.setText(lang.sezioniPagina);
        paginaLogin.setText(lang.sezioneAccedi);
        paginaListaAnime.setText(lang.sezioneListaAnime);
        paginaAggiungiAnime.setText(lang.sezioneAggiungiAnime);
        paginaRegistrazione.setText(lang.sezioneRegistrazione);
        
        if(lastNotify == null) return;
        // tradici la notifica, qualunque sia
        try {
            switch(lastNotify.getId()){
                case 4000:
                    notifica.setText(lang.erroreParametriClient);
                    break;
                case 4001:
                    notifica.setText(lang.errorePopolamento);
                    break;
                case 4002:
                    notifica.setText(lang.erroreNonCredenziali);
                    break;
                case 4003:
                    notifica.setText(lang.erroreAccesso);
                    break;
                case 4004:
                    notifica.setText(lang.erroreUsernameInesistente);
                    break;
                case 4005:
                    notifica.setText(lang.erroreNomeAnimeMancanteNellaRicerca);
                    break;
                case 4006:
                    notifica.setText(lang.erroreNessunAnimeSelezionato);
                    break;
                case 4007:
                    notifica.setText(lang.erroreTrailerNonDisponibile);
                    break;
                case 4008:
                    notifica.setText(lang.erroreAperturaTrailerSONonSupportato);
                    break;
                case 4009:
                    notifica.setText(lang.erroreUsernameGiaEsistente);
                    break;
                case 4010:
                    notifica.setText(lang.erroreDigitaCampiRegistrazione);
                    break;
                case 4011:
                    notifica.setText(lang.errorePasswordNonCoincidenti);
                    break;
                case 4012:
                    notifica.setText(lang.erroreAccessoPagina);
                    break;
                case 2001:
                    notifica.setText(lang.notificaInserimentoCorretto);
                    break;
                case 2501:
                    notifica.setText(lang.notificaListaVuota);
                    break;
                case 2502:
                    notifica.setText(lang.notificaRicercaAnimeVuota);
                    break;
                case 2503:
                    notifica.setText(lang.notificaAnimeGiaInLista);
                    break;
                case 5000:
                    notifica.setText(lang.erroreServerIrraggiungibile);
                    break;
                case 5001:
                    notifica.setText(lang.erroreNonImplementato);
                    break;
                case 5002:
                    notifica.setText(lang.erroreGenerico);
                    break;
                    
                default:
                    throw new CodificaNotificaErrataException();
                }
        } catch(CodificaNotificaErrataException e){
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Metodo per il cambio dell'interfaccia.
     * @param s 
     */
    protected void switchTo(Sezioni s) {
        
    }
    
    /**
     * Metodo per spegnere o accedere campi, bottoni, o altri oggetti di input.
     * @param accesi 
     */
    protected void semaforoCampi(boolean accesi){
        
    }
    
    /**
    * Metodo per nascondere la notifica.
    */
    protected void nascondiNotifica() {
        notifica.setStyle("visibility: hidden");
    }
    
    protected void mostraNotifica(String codice){
        try {
            Notifica n = codNotify.get(codice);
            if(n == null) throw new CodificaNotificaErrataException(); 
            lastNotify = n;
            notifica.setText(n.toString());
            notifica.getStyleClass().add("text-"+n.getColore());
            switch(n.getColore()){
                case "red":
                    notifica.getStyleClass().remove("text-green");
                    notifica.getStyleClass().remove("text-yellow");
                    break;
                case "green":
                    notifica.getStyleClass().remove("text-red");
                    notifica.getStyleClass().remove("text-yellow");
                    break;
                case "yellow":
                    notifica.getStyleClass().remove("text-red");
                    notifica.getStyleClass().remove("text-green");
                    break;
            }
            
            notifica.setStyle("visibility: visible");
            
        } catch (CodificaNotificaErrataException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Metodo privato per l'inizializzazione delle notifiche
     */
    private void inserisciCodificheNotifiche(){
        codNotify.put("4000", new Notifica(4000, lang.erroreParametriClient));
        codNotify.put("4001", new Notifica(4001, lang.errorePopolamento));
        codNotify.put("4002", new Notifica(4002, lang.erroreNonCredenziali));
        codNotify.put("4003", new Notifica(4003, lang.erroreAccesso));
        codNotify.put("4004", new Notifica(4004, lang.erroreUsernameInesistente));
        codNotify.put("4005", new Notifica(4005, lang.erroreNomeAnimeMancanteNellaRicerca));
        codNotify.put("4006", new Notifica(4006, lang.erroreNessunAnimeSelezionato));
        codNotify.put("4007", new Notifica(4007, lang.erroreTrailerNonDisponibile));
        codNotify.put("4008", new Notifica(4008, lang.erroreAperturaTrailerSONonSupportato));
        codNotify.put("4009", new Notifica(4009, lang.erroreUsernameGiaEsistente));
        codNotify.put("4010", new Notifica(4010, lang.erroreDigitaCampiRegistrazione));
        codNotify.put("4011", new Notifica(4011, lang.errorePasswordNonCoincidenti));
        codNotify.put("4012", new Notifica(4012, lang.erroreAccessoPagina));
        
        codNotify.put("2000", new Notifica(2000, "OK"));
        codNotify.put("2001", new Notifica(2001, lang.notificaInserimentoCorretto));
        codNotify.put("2501", new Notifica(2501, lang.notificaListaVuota));
        codNotify.put("2502", new Notifica(2502, lang.notificaRicercaAnimeVuota));
        codNotify.put("2503", new Notifica(2503, lang.notificaAnimeGiaInLista));
        
        codNotify.put("5001", new Notifica(5001, lang.erroreNonImplementato));
        codNotify.put("5000", new Notifica(5000, lang.erroreServerIrraggiungibile));
        codNotify.put("5002", new Notifica(5002, lang.erroreGenerico));
        
    }    
}
