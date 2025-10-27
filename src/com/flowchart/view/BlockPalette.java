package com.flowchart.view;

import com.flowchart.model.BlockType;
import javax.swing.*;
import java.awt.*;

/**
 * Pannello con la palette dei blocchi disponibili
 */
public class BlockPalette extends JPanel {
    private FlowchartCanvas canvas;

    public BlockPalette(FlowchartCanvas canvas) {
        this.canvas = canvas;
        setLayout(new GridLayout(0, 1, 5, 5));
        setPreferredSize(new Dimension(150, 0));
        setBorder(BorderFactory.createTitledBorder("Blocchi"));

        initializePalette();
    }

    private void initializePalette() {
        // Aggiungi pulsanti per ogni tipo di blocco
        for (BlockType type : BlockType.values()) {
            JButton button = createBlockButton(type);
            add(button);
        }

        // Aggiungi pulsante per eliminare blocco selezionato
        add(Box.createRigidArea(new Dimension(0, 20)));

        JButton deleteButton = new JButton("Elimina");
        deleteButton.setBackground(new Color(244, 67, 54));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> canvas.deleteSelectedBlock());
        add(deleteButton);

        // Aggiungi pulsante per pulire tutto
        JButton clearButton = new JButton("Pulisci Tutto");
        clearButton.setBackground(new Color(158, 158, 158));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler eliminare tutto?",
                "Conferma",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                canvas.clearAll();
            }
        });
        add(clearButton);
    }

    private JButton createBlockButton(BlockType type) {
        JButton button = new JButton("<html><center>" + type.getDisplayName() + "</center></html>");
        button.setBackground(Color.decode(type.getColor()));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText("Clicca per aggiungere un blocco " + type.getDisplayName());

        button.addActionListener(e -> canvas.addBlock(type));

        return button;
    }
}
