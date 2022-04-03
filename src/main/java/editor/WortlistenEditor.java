package editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class WortlistenEditor extends JFrame {
    //	für das Eingabefeld
    private JTextArea textfeld;
    //	für die Schaltflächen
    private JButton neuesWort, loeschen, beenden;
    //	für den Dateinamen
    private String dateiName, wort;
    //	für die Anzahl der Wörter
    private int anzahlWoerter = 0;
    //	boolean zum Datei Status
    private boolean geladen;
    //	ein Label für die Anzahl der Gepeicherten Wörter
    private JLabel gespeicherteWoerter;

    class MeinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
//			wurde auf Einlesen geklickt?
            if (e.getActionCommand().equals("lesen"))
                dateiLesen();
//			wurde auf Neues Wort gecklickt?
            if (e.getActionCommand().equals("neuesWort"))
                wortHinzufügen();
//			wurde auf Liste Löschen geklickt?
            if (e.getActionCommand().equals("löschen"))
                loeschen();
//			wurde auf Beenden geklickt?
            if (e.getActionCommand().equals("schließen"))
                beenden();
        }
    }

    //	der Konstuktor
    public WortlistenEditor(String titel) {
        super(titel);
        dateiName = "/wortliste_hangman.bin";
//		für das Panel mit den Schaltflächen und ein weiteres zum Zählen der gespeicherten Wörter
        JPanel southPanel, northpanel;
//		ein neues Eingabefeld erstellen
        textfeld = new JTextArea();
//		und für Nutzereingaben Sperren
        textfeld.setEditable(false);
//		die Schaltflächen einfügen und verknüpfen
        neuesWort = new JButton("Neues Wort");
        neuesWort.setActionCommand("neuesWort");
        loeschen = new JButton("Liste löschen");
        loeschen.setActionCommand("löschen");
        beenden = new JButton("Schließen");
        beenden.setActionCommand("schließen");

//		die Schaltflächen mit einem neuen Actionlistener verbinden
        MeinListener listener = new MeinListener();
        neuesWort.addActionListener(listener);
        loeschen.addActionListener(listener);
        beenden.addActionListener(listener);
//		ein label zur anzeige der gespeicherten Wortmenge
        gespeicherteWoerter = new JLabel();

//		ein BorderLayout anwenden
        setLayout(new BorderLayout());
//		das Eingabefeld mit Scrollbars
        add(new JScrollPane(textfeld), BorderLayout.CENTER);
//		ein Panel für die Schaltflächen
        southPanel = new JPanel();
//		ein Panel für die Schaltflächen
        southPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		wir lesen direkt die aktuellen Wörter ein...Sofern vorhanden
        dateiLesen();
//		und fügen die Schaltflächen hinzu
        southPanel.add(neuesWort);
        southPanel.add(loeschen);
        southPanel.add(beenden);
        add(southPanel, BorderLayout.SOUTH);
//		das gleiche nochmal für unser gespeicherteWoerter Label
        northpanel = new JPanel();
        northpanel.add(gespeicherteWoerter);
//		Das gespeicherteWoerter Label aktualisieren
        gespeicherteWoerter.setText("Aktuell gespeicherte Wörter: " + anzahlWoerter);

        add(northpanel, BorderLayout.NORTH);
//		Größe setzen, Standardverhalten zum schließen festlegen
        setMinimumSize(new Dimension(340, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		mittig auf dem Desktop positionieren
        setLocationRelativeTo(null);
//		und anzeigen
        setVisible(true);
    }

    //	die Methode zum einlesen einer Datei
    private void dateiLesen() {
        try {
//			die Datei mit lesezugriff öffnen
            RandomAccessFile datei = new RandomAccessFile(dateiName, "r");
//			die Anzahl der Wörter bzw. gespeicherter Anzahl lesen
            anzahlWoerter = datei.readInt();
//			Das gespeicherteWoerter Label aktualisieren
            gespeicherteWoerter.setText("Aktuell gespeicherte Wörter: " + anzahlWoerter);
//			"säubern" des Eingabefeldes
            textfeld.setText("");
//			es wird solange gelesen bis die "anzahlWoerter" erreicht ist
            for (int index = 0; index < anzahlWoerter; index++) {
//				den Zeiger verschieben, 2 Bytes überspringen um das Trennzeichen zu umgehen
                datei.skipBytes(2);
//				Daten aus der Datei lesen und in das Textfeld übertragen
                textfeld.append(datei.readUTF() + '\n');
                geladen = true;
            }
//			datei wieder schließen
            datei.close();
        } catch (IOException e) {
        }
    }

    //	die Methode fügt ein neues Wort hinzu
    private void wortHinzufügen() {
        wort = JOptionPane.showInputDialog(textfeld, "Bitte geben Sie ein neues Wort ein");
        if (wort != null) {
            textfeld.setText(wort);
//			Zähler für die Wörter erhöhen
            anzahlWoerter++;
            geladen = true;
            dateiSchreiben();
        } else
            JOptionPane.showMessageDialog(textfeld, "Sie haben keine Eingabe vorgenommen");
    }

    //	die Methode zum öffnen der Datei mit Lese- und Schreibzugriff
    private void dateiSchreiben() {
        try (RandomAccessFile datei = new RandomAccessFile(dateiName, "rw")) {
//			Zähler für die Wörter erhöhen
//			die Anzahl der Wörter steht immer am Anfang
            datei.writeInt(anzahlWoerter);
//			ans Ende der Datei springen
            datei.seek(datei.length());
//			das Trennzeichen einfügen
            datei.writeChar('*');
//			Wort aus dem Textfeld speichern
            datei.writeUTF(wort);
//			das Textfeld leeren
            textfeld.setText("");
//			und die Datei wieder schließen
            datei.close();
//			wenn die Datei vorhanden ist...aktualisieren wir die Ansicht
            if (geladen)
                dateiLesen();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Beim speichern ist ein Problem aufgetreten");
        }
    }

    //die Methode leert die Liste durch Löschen der Datei
    private void loeschen() {
        File datei = new File(dateiName);
        datei.delete();
        textfeld.setText("");
//		Das gespeicherteWoerter Label aktualisieren
        gespeicherteWoerter.setText("Aktuell gespeicherte Wörter: " + "0");
        JOptionPane.showMessageDialog(textfeld, "Die Liste wurde erfolgreich gelöscht");
    }

    //die Methode zum schließen des Editors
    private void beenden() {
        this.dispose();
    }
}

