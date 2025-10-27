package com.flowchart;

import com.flowchart.view.MainFrame;
import javax.swing.*;

/**
 * Classe principale per avviare l'applicazione
 */
public class Main {
    public static void main(String[] args) {
        // Imposta il look and feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se fallisce, usa il look and feel predefinito
            System.err.println("Impossibile impostare il look and feel del sistema: " + e.getMessage());
        }

        // Avvia l'applicazione nel thread dell'interfaccia grafica
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}
