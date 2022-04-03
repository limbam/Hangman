package de.simon.hangman;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Optional;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Score extends Stage {
    // für die Bühne
    private Stage owner;
    // für den obersten Knoten
    private GridPane rootNode;

    // für die Punkte
    private int punkte;

    // für die Liste
    // für die eigentliche Liste
    private Listenelement[] bestenliste;
    // für die Anzahl der Einträge
    private int anzahl;

    // für den Dateinamen
    private String dateiName;

    // die innere Klasse für ein Listenelement
    class Listenelement implements Comparable<Listenelement> {
        // die Instanzvariablen
        private int listePunkte;
        private String listeName;

        // der Konstruktor
        // er ruft die Methode setzeEintrag() auf
        public Listenelement(int listePunkte, String listeName) {
            setzeEintrag(listePunkte, listeName);
        }

        // die überschriebene Vergleichsmethode
        // die Rückgabewerte steuern die Sortierung
        @Override
        public int compareTo(Listenelement tempEintrag) {
            if (this.listePunkte < tempEintrag.listePunkte)
                return 1;
            if (this.listePunkte > tempEintrag.listePunkte)
                return -1;
            else
                return 0;
        }

        // die Methode setzt die Werte für einen Eintrag
        public void setzeEintrag(int listePunkte, String listeName) {
            this.listeName = listeName;
            this.listePunkte = listePunkte;
        }

        // die Methode liefert die Punkte
        public int getListePunkte() {
            return listePunkte;
        }

        // die Methode liefert den Namen
        public String getListeName() {
            return listeName;
        }
    }

    // der Konstruktor
    // er erhält das übergeordnete Fenster zugewiesen
    public Score(Stage fenster) {
        // den Konstruktor der übergeordneten Klasse aufrufen
        super();
        owner = fenster;
        // den Besitzer zuweisen
        initOwner(fenster);
        rootNode = new GridPane();
        // die Szene erzeugen
        // an den Konstruktor werden der oberste Knoten und die Größe übergeben
        Scene meineScene = new Scene(rootNode, 300, 300);
        // die Szene setzen
        setScene(meineScene);
        // der Dialog soll modal angezeigt werden
        initModality(Modality.WINDOW_MODAL);
        // und nur ein Schließensymbol haben
        initStyle(StageStyle.UTILITY);

        // die Punkte auf 0 setzen
        loeschePunkte();

        // die Bestenliste hat 10 Einträge
        anzahl = 10;

        // eine neue Instanz für die Bestenliste erzeugen
        bestenliste = new Listenelement[anzahl];
        // die Elemente initialisieren
        for (int index = 0; index < anzahl; index++)
            bestenliste[index] = new Listenelement(0, "Nobody");

        // den Dateinamen setzen
        dateiName = "score.dat";

        // ist die Datei score.dat vorhanden?
        File dateiTest = new File(dateiName);
        // wenn ja, lesen wir die Daten ein
        if (dateiTest.exists() == true)
            datenLesen();

    }

    // die Methode verändert die Punkte
    // Sie erhöht und verringert die aktuelle Punktzahl und gibt den aktuellen Wert
    // zurück
    // das Verringern erfolgt durch die Übergabe eines negativen Wertes
    public int veraenderePunkte(int punkte) {
        this.punkte = this.punkte + punkte;
        return this.punkte;
    }

    // die Methode setzt die Punkte zurück auf 0
    // für das HangMan-Spiel wird die Methode nicht zwingend benötigt
    public void loeschePunkte() {
        punkte = 0;
    }

    // die Methode setzt einen neuen Eintrag in die Liste
    // wenn ein neuer Eintrag erfolgt, wird true geliefert, sonst fals
    public boolean neuerEintrag() {
        String tempName;
        // wenn die aktuelle Punktzahl größer ist als der letzte Eintrag
        // in der Liste, wird der letzte Eintrag überschrieben und die Liste
        // neu sortiert
        if (punkte > bestenliste[anzahl - 1].getListePunkte()) {
            // den Namen beschaffen über einen Dialog
            TextInputDialog meinDialog = new TextInputDialog();
            meinDialog.setTitle("Gewonnen");
            meinDialog.setHeaderText("Herzlichen Glückwunsch");
            meinDialog.setContentText("Bitte geben Sie Ihren Namen ein: ");
            meinDialog.initOwner(owner);
            Optional<String> dialogEingabe = meinDialog.showAndWait();
            // wurde etwas eingegeben?
            // dann weisen wir es zu
            if (dialogEingabe.isPresent())
                tempName = dialogEingabe.get();
            else
                // sonst benutzen wir einen Standardnamen
                tempName = "Max Mustermann";
            // den neuen Eintrag setzen
            bestenliste[anzahl - 1].setzeEintrag(punkte, tempName);
            // die Liste sortieren
            Arrays.sort(bestenliste);
            // die sortierte Liste zurückschreiben
            datenSchreiben();
            return true;
        } else
            return false;
    }

    // die Methode zeigt die Liste an
    // dazu werden die einzelnen Einträge geschrieben
    // und die Oberfläche dann angezeigt
    public void listeZeigen() {
        // den Titel setzen
        setTitle("Bestenliste");
        // die Überschriften einfügen
        rootNode.add(new Label("Punkte"), 0, 0);
        rootNode.add(new Label("Name"), 1, 0);
        // und jetzt die Einträge aus der Liste
        for (int index = 0; index < anzahl; index++) {
            // erst die Punkte
            rootNode.add(new Label(Integer.toString(bestenliste[index].getListePunkte())), 0, index + 1);
            // dann der Name
            rootNode.add(new Label(bestenliste[index].getListeName()), 1, index + 1);
        }
        // anzeigen und abwarten
        showAndWait();
    }

    // die Methode schreibt die Bestenliste komplett in eine binäre Datei score.dat
    public void datenSchreiben() {
        // die Datei zum Lesen und Schreiben öffnen
        try (RandomAccessFile datei = new RandomAccessFile(dateiName, "rw")) {
            // die Daten in die Datei schreiben
            for (int index = 0; index < anzahl; index++) {
                // erst die Punkte
                datei.writeInt(bestenliste[index].getListePunkte());
                // dann den Namen
                datei.writeUTF(bestenliste[index].getListeName());
            }
        } catch (IOException e) {
            System.out.println("Beim Schreiben der Bestenliste ist ein Problem aufgetreten");
        }
    }

    // die Methode liest die Bestenliste komplett aus einer binäre Datei score.dat
    public void datenLesen() {
        // zum leichteren Verarbeiten
        String tempName;
        int tempPunkte;
        // die Datei zum Lesen öffnen
        try (RandomAccessFile datei = new RandomAccessFile(dateiName, "r")) {
            // die Daten aus der Datei lesen
            for (int index = 0; index < anzahl; index++) {
                // erst die Punkte
                tempPunkte = datei.readInt();
                // dann den Namen
                tempName = datei.readUTF();
                // und nun zuweisen
                bestenliste[index].setzeEintrag(tempPunkte, tempName);
            }
        } catch (IOException e) {
            System.out.println("Beim Laden der Bestenliste ist ein Problem aufgetreten");
        }
    }
}
