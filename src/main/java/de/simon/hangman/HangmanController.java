package de.simon.hangman;

import editor.WortlistenEditor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.RandomAccessFile;

public class HangmanController {
    //das Kombinationsfeld
    @FXML
    private ComboBox<String> auswahl;
    //die Labels für die ausgaben
    @FXML
    private Label ausgabeText;
    @FXML
    private Label anzVersuche;
    //	das Punktelabel
    @FXML
    private Label punktAusgabe;
    //für die Zeichenfläche
    @FXML
    private Canvas zeichenflaeche;

    //ein Array mit Zeichenketten(!) für die Buchstaben
    private String[] zeichen = new String[26];
    //ein StringBuilder für die Darstellung des Suchwortes
    private StringBuilder anzeige;
    //	ein String für das gesuchte Wort im Klartext
    private String suchwort;
    //	für die verbleinden Durchläufe
    private int restDurchlaeufe;
    //	für die Anzahl der Fehler
    private int fehler;
    //	für den Grafikkontext
    private GraphicsContext gc;
    //	für die Punkte
    private Score spielpunkte;
    //	für die Stage
    private Stage meineStage;

    //	die Methode zum Beenden
    @FXML
    protected void beendenKlick(ActionEvent event) {
        Platform.exit();
    }

    //	die Methode zur Auswahl aus dem Kombinationsfeld
    @FXML
    protected void auswahlNeu(ActionEvent event) {
//		der aktuell ausgewählte Eintrag wird übergeben und ausgewertet
        pruefen(auswahl.getSelectionModel().getSelectedItem());
//		ist das Spiel zu Ende oder nicht?
        gewinnerOderNicht();
    }

    //	die Methode Ruft den Wortlisten Editor auf
    @FXML
    protected void wortListe() {
        new WortlistenEditor("HangMan Wortlisten Editor");
    }

    //	die Methode setzt die Initialwerte
    @FXML
    void initialize() {
        int tempIndex = 0;
//		es geht los mit 9 verbleibenden Durchläufen
        restDurchlaeufe = 9;
//		die restlichen Durchläufe anzeigen
        anzVersuche.setText(Integer.toString(restDurchlaeufe));

//		die Liste für das Kombinationsfeld füllen
        for (char temp = 'a'; temp <= 'z'; temp++) {
            zeichen[tempIndex] = Character.toString(temp);
            tempIndex++;
        }
        auswahl.getItems().addAll(zeichen);
//		ein Wort ermitteln
        neuesWort();

//		den grafikkontext beschaffen
        gc = zeichenflaeche.getGraphicsContext2D();
    }

    //	die Methode ermittelt zufällig ein Wort aus der Datei wortliste_hangman.bin
    private void neuesWort() {
//		eine Variable um die Anzahl der eingelesenen Wörter zu speichern
        int woerter = 0;
//		eine Variable um eine zufällige Zahl zu speichern
        int zufall = 0;
//		Datei öffnen
        try (RandomAccessFile datei = new RandomAccessFile("/wortliste_hangman.bin", "r")) {
//			anzahl der Gesamten Wörter einlesen
            int anzahlWoerter = datei.readInt();
//			eine zufällige zahl aus der anzahl der Wörter wählen
            zufall = (int) (Math.random() * anzahlWoerter);
//			die Datei solange durchgehen bis die zufällige Zahl erreicht ist
            while (woerter <= zufall) {
//				den Zeiger verschieben, 2 Bytes überspringen um das Trennzeichen zu umgehen
                datei.skipBytes(2);
//				und das Suchwort setzen
                suchwort = (datei.readUTF());
//				zähler erhöhen
                woerter++;
            }
//			datei wieder Schließen
            datei.close();
//		das Suchwort übergeben
            anzeige = new StringBuilder(suchwort);
//		alle Zeichen in der Anzeige ersetzen durch *
            for (int zeichen = 0; zeichen < suchwort.length(); zeichen++)
                anzeige.setCharAt(zeichen, '*');
//		die Sternchen anzeigen
            ausgabeText.setText(anzeige.toString());
        } catch (

                Exception e) {
            Alert meinAlert = new Alert(Alert.AlertType.INFORMATION, "Bitte fügen Sie Wörter zur Wortliste hinzu");
            meinAlert.setHeaderText("Beim Laden der Datei ist ein Problem aufgetreten");
            meinAlert.showAndWait();
        }
    }

    //		die Methode zum Prüfen
    private void pruefen(String auswahlZeichen) {
        char zeichen;
        int treffer = 0;
//		das ausgewählte Zeichen aus dem Kombinationsfeld umbauen
        zeichen = auswahlZeichen.charAt(0);
//		gibt es das Zeichen auch im Suchwort?
//		dabei vergleichen wir nur die Kleinbuchstaben
        treffer = suchwort.toLowerCase().indexOf(zeichen);
//		wenn wir nichts gefunden haben
        if (treffer < 0) {
//			1 von den verbleinden Durchläufen abziehen
            restDurchlaeufe--;
//			die restlichen Durchläufe anzeigen
            anzVersuche.setText(Integer.toString(restDurchlaeufe));
//			die Fehler für die Anzeige erhöhen und den Galgen zeichnen
            erhoeheFehler();
//			einen Punkt abziehen
            punktAusgabe.setText(Integer.toString(spielpunkte.veraenderePunkte(-1)));
        } else {
//			nach weiteren Vorkommen suchen
            while (treffer >= 0) {
//				das Zeichen aus der entsprechenden Position im Suchwort anzeigen
                anzeige.setCharAt(treffer, suchwort.charAt(treffer));
//				treffer erhöhen und dann weitersuchen
                treffer++;
                treffer = suchwort.toLowerCase().indexOf(zeichen, treffer);
//				Punkte erhöhen
                punktAusgabe.setText(Integer.toString(spielpunkte.veraenderePunkte(+5)));
            }
//			das geänderte Wort anzeigen
            ausgabeText.setText(anzeige.toString());
        }
    }

    private void gewinnerOderNicht() {
//		ende steuert, ob das Spiel zu Ende ist
//		nur dann wird die Liste geprüft und die Anwendung geschlossen
        boolean ende = false;
//		die Linienbreite auf 1 setzen
        gc.setLineWidth(1);
//		ist das Spiel zu Ende?
        if (restDurchlaeufe == 0) {
            Alert verlorenAlert = new Alert(Alert.AlertType.INFORMATION);
            verlorenAlert.setTitle("Spiel Ende");
            verlorenAlert.setHeaderText("Leider Verloren");
            verlorenAlert.setContentText("Das gesuchte Wort war: " + suchwort);
            verlorenAlert.showAndWait();
            Platform.exit();
        }
//		ist das Wort erraten worden?
        if (anzeige.toString().equals(suchwort)) {
//			pro verbleibendem Durchlauf gibt es noch zehn Punkte extra
            spielpunkte.veraenderePunkte(restDurchlaeufe * 10);
            gc.strokeText("Hurra! Sie haben gewonnen", 20, 100);
            ende = true;
        }
        if (ende == true) {
//			hat es für einen neuen Eintrag in der Bestenliste gereicht?
            if (spielpunkte.neuerEintrag() == true)
                spielpunkte.listeZeigen();
            Platform.exit();
        }
    }

    //	Fehler hochzählen und den Galgen zeichnen
    private void erhoeheFehler() {
        fehler = fehler + 1;
        gc.setLineWidth(4);

//		je nach Wert von fehler zeichnen
        switch (fehler) {
            case 1:
                gc.strokeLine(10, 10, 10, 200);
                break;
            case 2:
                gc.strokeLine(10, 10, 100, 10);
                break;
            case 3:
                gc.strokeLine(40, 10, 10, 40);
                break;
            case 4:
                gc.strokeLine(100, 10, 100, 50);
                break;
            case 5:
                gc.strokeLine(70, 50, 130, 50);
                break;
            case 6:
                gc.strokeLine(130, 50, 130, 110);
                break;
            case 7:
                gc.strokeLine(130, 110, 70, 110);
            case 8:
                gc.strokeLine(70, 110, 70, 50);
                break;
            case 9:
                gc.strokeLine(0, 200, 20, 200);
                break;
        }
    }

    //	die Methode setzt die Bühne auf den übergebenen Wert
    public void setStage(Stage meineStage) {
        this.meineStage = meineStage;
        spielpunkte = new Score(meineStage);
    }
}
