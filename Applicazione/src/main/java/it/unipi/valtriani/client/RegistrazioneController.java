package it.unipi.valtriani.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import static it.unipi.valtriani.client.ScheletroController.user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Classe controller relativa al file registrazione.fxml
 * @author Lorenzo Valtriani (bobo)
 */
public class RegistrazioneController extends ScheletroController {
    @FXML private Label labelUsername;
    @FXML private Label labelInventaPassword;
    @FXML private Label labelConfermaPassword;
    @FXML private TextField username;
    @FXML private PasswordField inventaPassword;
    @FXML private PasswordField confermaPassword;
    @FXML private Button bottoneRegistrati;
    @FXML private Text errore;
    @FXML private Text consiglioRegistrazione;
    @FXML private Text sottotitolo;
    
    /**
     * Inizializza l'interfaccia.
     */
    @FXML
    @Override
    protected void initialize(){
        super.initialize();   
        traduci(lingua);
    } 
    
    /**
     * Quando viene cliccato il bottone per registrarsi.
     * L'utente vuole registrarsi, nel caso in cui non abbia inserito tutti i dati richiesti, viene mostrata uuna notifica.
     * Nel caso abbia inserito due password uguali, viene mostrata una notifica.
     * Nel caso l'username sia già in uso, viene mostrata una notifica.
     * Nel caso la registrazione avviene con successo, l'utente sarà reindirizzato alla interfaccoa anime.fxml
     */
    @FXML
    private void registrati() {
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
        // controllo che abbia inserito tutti i dati richiesti
        if(username.getText().equals("") || inventaPassword.getText().equals("") || confermaPassword.getText().equals("")){
            mostraNotifica("4010");
            return;
        }
        // controllo che la password sia uguale a quella confermata
        if(!inventaPassword.getText().equals(confermaPassword.getText())){
            mostraNotifica("4011");
            return;
        }
        
        Utente u = new Utente(username.getText(), inventaPassword.getText());
        
        
        // Task per la registrazione
        Task registrazioneTask;
        registrazioneTask = new Task<Void>(){
            @Override
            public Void call(){
                try {
                    Gson gson = new Gson();
                    String utenteData = gson.toJson(u);
                    URL url = new URL("http://localhost:8080/utente/registrazione");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    // viene inviata una put per registrare l'utente e i dati vengono inviati del body serializzati in json poichè in un futuro
                    // potrebbe essere che i dati richiesti possano aumentare, e quindi basterebbe cambiare la struttura della classe
                    con.setRequestMethod("PUT"); 
                    
                    con.setDoOutput(true); // altrimenti non riusciamo a scrivere il cobtenuto nel flusso in uscita
                    con.setRequestProperty("Content-Type", "application/json"); // dichiariamo che inviamo json
                    con.setRequestProperty("Accept", "application/json"); // dichiariamo che riceviamo json
                    
                    // scriviamo il corpo della richiesta
                    try(OutputStream os = con.getOutputStream()) {
                        byte[] input = utenteData.getBytes("utf-8");
                        os.write(input, 0, input.length);			
                    }
                    
                    // leggiamo la risposta del server
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    
                    // deserializzazione della risposta del server
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();
                     
                    // gestiamo la risposta del server
                    Platform.runLater(new Runnable(){
                       @Override
                       public void run(){
                           semaforoCampi(true);
                           // Leggi la risposta del server
                            switch(response){
                                case "4009":    // Username già in uso
                                    mostraNotifica(response);
                                    break;
                                case "2000":    // Registrazione avvenuta con successo
                                    user = u.username;
                                    try {
                                        App.setRoot("anime"); // cambio dell'interfaccia
                                    } catch (IOException ex) {
                                        System.out.println(ex.getMessage());
                                    }
                                    break;

                                default:        // Risposta sconosciuta, quindi non implementata
                                    mostraNotifica("5001");
                            }
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
       new Thread(registrazioneTask).start();
    }
    
    /**
     * Ovverride del metodo switchTo.
     * Passa ad un altra interfaccia dell'applicazione
     * @param s
     */
    @Override
    protected void switchTo(Sezioni s){
        // Rimuovi tutte le notifiche che possono esserci
        nascondiNotifica();
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
                case Registrazione:
                    nascondiNotifica();
                    break;
                case Login:
                    App.setRoot("login");
                    break;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
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
        labelInventaPassword.setText(lang.inventaPassword);
        labelConfermaPassword.setText(lang.confermaPassword);
        bottoneRegistrati.setText(lang.bottoneRegistrati);
        labelUsername.setText(lang.username);
        consiglioRegistrazione.setText(lang.consiglioRegistrazione);
        sottotitolo.setText(lang.sottotitolo);
    }
    
    /**
     * Override del metodo semaforoCampi.
     */
    @Override
    protected void semaforoCampi(boolean accesi){
        super.semaforoCampi(accesi);
        bottoneRegistrati.setDisable(!accesi); 
        username.setDisable(!accesi);
        inventaPassword.setDisable(!accesi);
        confermaPassword.setDisable(!accesi);
    }
}