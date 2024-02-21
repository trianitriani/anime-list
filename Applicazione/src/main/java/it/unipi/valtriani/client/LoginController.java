package it.unipi.valtriani.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static it.unipi.valtriani.client.ScheletroController.user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Classe controller relativa al file login.fxml
 * @author Lorenzo Valtriani (bobo)
 */
public class LoginController extends ScheletroController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button bottoneAccedi;
    @FXML private Text consiglioLogin;
    @FXML private Text sottotitolo;
    @FXML private Button bottoneDatabase;
    
    private String usernameValue;
    private String pswValue;
    
    // Task per popolare il database
    Task taskPopolaDataBase = new Task<Void>(){
            @Override
            public Void call() {
                semaforoCampi(false);
                try {
                    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "root")){
                        Statement st = c.createStatement(); 
                        st.executeUpdate("DROP DATABASE IF EXISTS d615550;");
                        st.executeUpdate("CREATE DATABASE d615550;");
                        st.executeUpdate("USE d615550;");
                        st.executeUpdate("CREATE TABLE Utente( " +
                                         "  id INT AUTO_INCREMENT PRIMARY KEY," +
                                         "  username VARCHAR(100) NOT NULL," +
                                         "  password VARCHAR(100) NOT NULL" +
                                         ");");
                        
                        st.executeUpdate("CREATE TABLE Anime (" +
                                         "  id INT AUTO_INCREMENT NOT NULL," +
                                         "  nome VARCHAR(100)  NOT NULL," +
                                         "  genere VARCHAR(50) NOT NULL," +
                                         "  episodi INT," +
                                         "  durata VARCHAR(50) NOT NULL," +
                                         "  trailer TEXT," +
                                         "  PRIMARY KEY(id)" +
                                         ");");
                        
                        st.executeUpdate("CREATE TABLE ListaAnime (" +
                                         "  idutente INT NOT NULL," +
                                         "  idanime INT NOT NULL," +
                                         "  PRIMARY KEY (idutente, idanime)," +
                                         "  FOREIGN KEY (idutente) REFERENCES Utente(id)" +
                                         "  ON UPDATE CASCADE ON DELETE CASCADE," +
                                         "  FOREIGN KEY (idanime) REFERENCES Anime(id)" +
                                         "  ON UPDATE CASCADE ON DELETE CASCADE" +
                                         ");");
                        st.executeUpdate("INSERT INTO Utente(Username, Password)" +
                                "VALUES (\"bobo\", \"$2a$12$2UwLW7lewrubKk0I4tc70eqlAAUHwIgkH.ifgexAajcsO0Xt/DG.O\")");
                        
                        // Adesso dobbiamo ottenere i dati dalla API
                        URL url = new URL("https://api.jikan.moe/v4/anime");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        StringBuilder content = new StringBuilder();
                        // Otteniamo il Json da analizzare
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                content.append(inputLine);
                            }
                        }
                        Gson gson = new Gson();
                        JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                        JsonObject rootObject = json.getAsJsonObject();
                        // Otterremo qua un array di Json
                        JsonArray anime = (JsonArray) rootObject.get("data");
                        
                        // Creazione della query preparata
                        try (PreparedStatement ps = c.prepareStatement("INSERT INTO Anime(nome, genere, episodi, durata, trailer) VALUES (?, ?, ?, ?, ?);")){
                            for(int i=0; i<anime.size(); i++){
                                // Ottenimento del singolo oggetto json
                                JsonObject an = (JsonObject) anime.get(i);

                                // Adesso inseriamo i singoli anime all'interno del db alternando i proprietari
                                ps.setString(1, an.get("title").getAsString());
                                ps.setString(2, ((JsonObject)((JsonArray) an.get("genres")).get(0)).get("name").getAsString());
                                
                                String episodes = an.get("episodes").toString();  // Alcuni anime hanno il numero degli episodi nullo                   
                                if(episodes.equals("null")) ps.setInt(3, 0);
                                else ps.setInt(3, an.get("episodes").getAsInt());
                                
                                ps.setString(4, an.get("duration").getAsString());

                                String trailer = an.get("trailer").getAsJsonObject().get("url").toString();
                                if(trailer.equals("null")) ps.setString(5, null);
                                else ps.setString(5, an.get("trailer").getAsJsonObject().get("url").getAsString());

                                ps.executeUpdate();
                            }
                        }
                        
                        // Ora metto a null i valori che sono a zero della colonna episodi
                        st.executeUpdate("UPDATE Anime " +
                                         "SET episodi = null " +
                                         "WHERE episodi = 0;"
                                        );
                        
                        // Ora leghiamo secondo la tabella ListaAnime
                        try (PreparedStatement ps = c.prepareStatement("INSERT INTO ListaAnime(idutente, idanime) VALUES (?, ?);")){
                            for(int i=0; i<anime.size(); i++){
                                ps.setInt(1, 1);
                                ps.setInt(2, i+1);
                                ps.executeUpdate();
                                if(i == 6) break;
                            }
                            
                            popolato = true;
                            semaforoCampi(true);
                        }
                    }
                } catch (SQLException ex) {
                    System.out.println("Eccezione SQL: " + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("Eccezione IO: " + ex.getMessage());
                }
                return null;
            }
        }; 
    
    /**
     * Inizializza l'interfaccia.
     * Controlla se il database esiste realmente o meno, nel caso esistesse mette popolato a true
     */
    @FXML
    @Override
    protected void initialize(){
        super.initialize();
        traduci(lingua);
        
        // Si controlla se esiste o meno il database
        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "root")){
            Statement st = c.createStatement(); 
            st.executeUpdate("USE d615550;");
            // Se non lancia alcuna eccezione allora esiste il database
            popolato = true;
        } catch (SQLException ex) {
            // Non esiste il database
            popolato = false;
        }
    }
    
    /**
     * Quando si cerca di accedere col proprio account.
     * Nel caso in cui username o password non sono state digitate, ne da notifica.
     * Nel caso in cui l'username contenga spazi o le credenziali sono errate, ne da notifica.
     * Altrimenti la login va a buon fine e si viene reindirizzati alla interfaccia anime.fxml.
     */
    @FXML
    private void accedi() {
        // togliamo le notifiche
        nascondiNotifica();
        
        // se il database non è popolato allora dai errore e di di popolarlo
        if(!popolato){
            mostraNotifica("4001");
            return;
        }
        
        usernameValue = username.getText();
        pswValue = password.getText();
        
        // Se anche uno non è stato digitato non fare una richiesta al server
        if(usernameValue.equals("") || pswValue.equals("")){
            mostraNotifica("4002");
            return;
        }
               
        // Adesso per sistemare alcuni errori relativi a possibili spazi all'interno dell'username e della password agiamo così
        // Per l'username neghiamo la possibilità di inserire uno spazio
        if(usernameValue.contains(" ")){
            // Diciamo all'utente che le credenziali sono errate
            mostraNotifica("4003");
            return;
        }
        
        // Per la password gestiamo la cosa codificandola nella richiesta stessa con dei '+'
        pswValue = URLEncoder.encode(pswValue, StandardCharsets.UTF_8);
        Task taskLogin = new Task<Void>(){
            @Override
            public Void call(){
                try {
                    URL url = new URL("http://localhost:8080/utente/login?user="+usernameValue+"&psw="+pswValue);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    // utilizziamo una get poichè i valori da passare saranno sempre username e password
                    con.setRequestMethod("GET");
                    
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
                            semaforoCampi(true);
                            // Arrivata la risposta del server, allora agiamo di conseguenza
                            switch(response){
                                case "4003": // Password errata 
                                case "4004": // L'username che è stato inserito non si trova nel db
                                    mostraNotifica(response);
                                    break;
                                case "2000": // Login correttamente svolto, passa alla pagina personale dell'utente
                                    user = usernameValue;
                                    try {
                                        App.setRoot("anime"); // cambio dell'interfaccia
                                    } catch (IOException ex) {
                                        System.out.println(ex.getMessage());
                                    }
                                    break;
                                default:     // Risposta sconosciuta, quindi non implementata
                                    mostraNotifica("5001");
                            }
                        } 
                    });
                    
                } catch (IOException e){
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
        new Thread(taskLogin).start();
    }
    
    /**
     * Popola il database.
     */
    @FXML
    private void popolaDataBase() {
        nascondiNotifica();
        new Thread(taskPopolaDataBase).start();
    }
    
    /**
     * Ovverride del metodo traduci.
     * Traduce i testi dei vari oggetti che si trovano in questa classe
     * @param lingua 
     */
    @Override
    protected void traduci(String lingua){
        super.traduci(lingua);
        // Inseriamo i testi nella lingua corretta
        bottoneAccedi.setText(lang.bottoneAccedi);
        consiglioLogin.setText(lang.consiglioLogin);
        sottotitolo.setText(lang.sottotitolo);
    }
    
    /**
     * Ovverride del metodo switchTo.
     * Passa ad un altra interfaccia dell'applicazione
     * @param s
     */
    @Override
    protected void switchTo(Sezioni s) {
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
        if(!popolato){ // nel caso non sia popolato, non può fare nulla se non prima popolare
            mostraNotifica("4001");
            return;
        }
        
        try {
            if(null != s) switch (s) {
                case ListaAnime:
                case AggiungiAnime:
                    // se i valori di utente non sono stati già inseriti (l'utente non si è loggato)
                    if(user == null || user.equals("")){
                        mostraNotifica("4012");
                    // se i valori sono già stati inseriti (l'utente si è già loggato precedentemente)
                    } else if(s == Sezioni.ListaAnime) App.setRoot("anime");
                    else App.setRoot("aggiungi");
                    break;
                case Login:
                    nascondiNotifica();
                    break;
                case Registrazione:
                    App.setRoot("registrazione");
                    break;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
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
        bottoneAccedi.setDisable(!accesi); 
        username.setDisable(!accesi);
        password.setDisable(!accesi);
    }
}
