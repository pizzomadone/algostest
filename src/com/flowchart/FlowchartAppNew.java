package com.flowchart;

import com.flowchart.view.MainFrameNew;
import javax.swing.SwingUtilities;

/**
 * Main class per la nuova versione con layout verticale automatico
 */
public class FlowchartAppNew {
    public static void main(String[] args) {
        // Imposta il Look and Feel del sistema
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Avvia l'applicazione
        SwingUtilities.invokeLater(() -> {
            new MainFrameNew();
        });
    }
}
