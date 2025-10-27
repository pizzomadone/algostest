package com.flowchart.view;

import com.flowchart.executor.FlowchartExecutor;
import com.flowchart.model.FlowchartModel;
import javax.swing.*;
import java.awt.*;

/**
 * Finestra principale dell'applicazione
 */
public class MainFrame extends JFrame {
    private FlowchartModel model;
    private FlowchartCanvas canvas;
    private BlockPalette palette;
    private ControlPanel controlPanel;
    private FlowchartExecutor executor;

    public MainFrame() {
        setTitle("Editor Diagrammi a Blocchi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        setupMenuBar();

        setVisible(true);
    }

    private void initializeComponents() {
        // Inizializza il modello
        model = new FlowchartModel();

        // Inizializza il canvas
        canvas = new FlowchartCanvas(model);
        JScrollPane canvasScroll = new JScrollPane(canvas);
        canvasScroll.setPreferredSize(new Dimension(800, 600));

        // Inizializza l'executor
        executor = new FlowchartExecutor(model);

        // Inizializza la palette
        palette = new BlockPalette(canvas);

        // Inizializza il pannello di controllo
        controlPanel = new ControlPanel(executor, canvas);
        controlPanel.setPreferredSize(new Dimension(300, 0));

        // Layout
        setLayout(new BorderLayout());

        // Pannello sinistro con palette
        add(palette, BorderLayout.WEST);

        // Canvas centrale
        add(canvasScroll, BorderLayout.CENTER);

        // Pannello destro con controlli
        add(controlPanel, BorderLayout.EAST);

        // Pannello info in basso
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JLabel infoLabel = new JLabel(
            "  Click sinistro: seleziona/trascina | Click destro: crea connessione | Doppio click: modifica testo  "
        );
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(infoLabel);

        return panel;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu File
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("Nuovo");
        newItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler creare un nuovo diagramma?",
                "Nuovo Diagramma",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                canvas.clearAll();
            }
        });

        JMenuItem exitItem = new JMenuItem("Esci");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Menu Aiuto
        JMenu helpMenu = new JMenu("Aiuto");

        JMenuItem aboutItem = new JMenuItem("Info");
        aboutItem.addActionListener(e -> showAboutDialog());

        JMenuItem instructionsItem = new JMenuItem("Istruzioni");
        instructionsItem.addActionListener(e -> showInstructionsDialog());

        helpMenu.add(instructionsItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Editor Diagrammi a Blocchi\n" +
            "Versione 1.0\n\n" +
            "Un'applicazione per creare ed eseguire\n" +
            "algoritmi con diagrammi a blocchi.\n\n" +
            "© 2024",
            "Info",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showInstructionsDialog() {
        String instructions =
            "COME USARE L'APPLICAZIONE:\n\n" +
            "1. CREARE BLOCCHI:\n" +
            "   - Clicca sui pulsanti nella palette a sinistra\n" +
            "   - I blocchi disponibili sono:\n" +
            "     * Inizio: punto di partenza dell'algoritmo\n" +
            "     * Fine: punto di termine dell'algoritmo\n" +
            "     * Processo: esegue operazioni (es: x = 5)\n" +
            "     * Decisione: condizione if (es: x > 0)\n" +
            "     * Input: legge un valore dall'utente\n" +
            "     * Output: mostra un valore all'utente\n\n" +
            "2. MODIFICARE BLOCCHI:\n" +
            "   - Doppio click su un blocco per modificare il testo\n" +
            "   - Trascina i blocchi per spostarli\n\n" +
            "3. COLLEGARE BLOCCHI:\n" +
            "   - Click destro sul blocco sorgente\n" +
            "   - Trascina fino al blocco destinazione\n" +
            "   - Rilascia per creare la connessione\n" +
            "   - Per blocchi Decisione, scegli SI o NO\n\n" +
            "4. ESEGUIRE:\n" +
            "   - 'Esegui Tutto': esegue l'intero algoritmo\n" +
            "   - 'Passo-Passo': esegue un blocco alla volta\n" +
            "   - 'Stop': ferma l'esecuzione\n\n" +
            "5. SINTASSI:\n" +
            "   - Processo: x = 10, y = x + 5\n" +
            "   - Decisione: x > 0, y == 5, x != y\n" +
            "   - Input: nome_variabile\n" +
            "   - Output: variabile o \"testo\"\n\n" +
            "ESEMPI:\n" +
            "- Somma: Inizio → Input(a) → Input(b) → Processo(c=a+b) → Output(c) → Fine\n" +
            "- Controllo: Inizio → Input(x) → Decisione(x>0) → [SI]Output(\"Positivo\") → Fine\n" +
            "                                                  → [NO]Output(\"Negativo\") → Fine";

        JTextArea textArea = new JTextArea(instructions);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Istruzioni",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
