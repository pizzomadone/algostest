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
        Color deleteColor = new Color(198, 40, 40);
        deleteButton.setBackground(deleteColor);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(deleteColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        deleteButton.addChangeListener(e -> {
            if (deleteButton.getModel().isPressed()) {
                deleteButton.setBackground(deleteColor.darker());
            } else if (deleteButton.getModel().isRollover()) {
                deleteButton.setBackground(deleteColor.brighter());
            } else {
                deleteButton.setBackground(deleteColor);
            }
        });
        deleteButton.addActionListener(e -> canvas.deleteSelectedBlock());
        add(deleteButton);

        // Aggiungi pulsante per pulire tutto
        JButton clearButton = new JButton("Pulisci Tutto");
        Color clearColor = new Color(97, 97, 97);
        clearButton.setBackground(clearColor);
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setContentAreaFilled(true);
        clearButton.setBorderPainted(false);
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(clearColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        clearButton.addChangeListener(e -> {
            if (clearButton.getModel().isPressed()) {
                clearButton.setBackground(clearColor.darker());
            } else if (clearButton.getModel().isRollover()) {
                clearButton.setBackground(clearColor.brighter());
            } else {
                clearButton.setBackground(clearColor);
            }
        });
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
        JButton button = new JButton(type.getDisplayName());
        Color bgColor = Color.decode(type.getColor());

        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setToolTipText("Clicca per aggiungere un blocco " + type.getDisplayName());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Forza il colore anche durante hover e press
        button.addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                button.setBackground(bgColor.darker());
            } else if (button.getModel().isRollover()) {
                button.setBackground(bgColor.brighter());
            } else {
                button.setBackground(bgColor);
            }
        });

        button.addActionListener(e -> canvas.addBlock(type));

        return button;
    }
}
