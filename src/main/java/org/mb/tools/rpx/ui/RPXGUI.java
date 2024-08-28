package org.mb.tools.rpx.ui;

import org.mb.tools.rpx.models.RekordboxPlaylistParam;
import org.mb.tools.rpx.services.export.ExportService;
import org.mb.tools.rpx.services.export.ExportServiceTxtImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Application GUI
 */
public class RPXGUI extends JFrame {

    private final JPanel panel;
    private List<RekordboxPlaylistParam> rekordboxPlaylistParamList;

    public RPXGUI() {
        rekordboxPlaylistParamList = new ArrayList<>();
        setTitle("RPX - Rekordbox Playlist Exporter");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500, 200);

        panel = new JPanel();
        initPanel();
    }

    private void initPanel() {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton selectTxtFilesButton = new JButton("SELECT ALL TXT PLAYLIST FILES TO EXPORT");
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(selectTxtFilesButton, gbc);

        selectTxtFilesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                reloadPanel(selectedFiles, "txt");
            }
        });

        add(panel);
    }

    private void reloadPanel(File[] selectedFiles, String inputFormat) {
        panel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel label = new JLabel("Selected playlist:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label, gbc);

        JTextField[] filePathFields = new JTextField[selectedFiles.length];
        JCheckBox[] checkBoxes = new JCheckBox[selectedFiles.length];
        for (int i = 0; i < selectedFiles.length; i++) {
            JTextField filePathField = new JTextField(selectedFiles[i].getAbsolutePath());
            filePathField.setEditable(false);
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            panel.add(filePathField, gbc);
            filePathFields[i] = filePathField;

            JCheckBox checkBox = new JCheckBox("Keep tracks order");
            gbc.gridx = 1;
            gbc.gridy = i + 1;
            panel.add(checkBox, gbc);
            checkBoxes[i] = checkBox;
        }

        JButton exportButton = getExportButton(selectedFiles, filePathFields, checkBoxes, "txt");
        gbc.gridx = 0;
        gbc.gridy = selectedFiles.length + 1;
        gbc.gridwidth = 2;
        panel.add(exportButton, gbc);

        pack();
    }

    private JButton getExportButton(File[] selectedFiles, JTextField[] filePathFields, JCheckBox[] checkBoxes,
                                    String inputFormat) {
        JButton exportButton = new JButton("EXPORT SELECTED PLAYLIST");
        exportButton.addActionListener(e -> {
            for (int i = 0; i < selectedFiles.length; i++) {
                RekordboxPlaylistParam param = new RekordboxPlaylistParam();
                param.setPlaylistPath(filePathFields[i].getText());
                param.setMaintainPlaylistOrder(checkBoxes[i].isSelected());
                rekordboxPlaylistParamList.add(param);
            }
            try {
                ExportService exportService;
                switch (inputFormat) {
                    // gestire qui altri formati (es. M3U8)
                    default:
                        exportService = new ExportServiceTxtImpl();
                        break;
                }
                exportService.exportPlaylists(rekordboxPlaylistParamList);
                JOptionPane.showMessageDialog(null, "Operation successfully done!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                panel.removeAll();
                resetPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "An error occurred during operation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return exportButton;
    }

    private void resetPanel() {
        panel.removeAll();
        initPanel();
        revalidate();
        repaint();
        rekordboxPlaylistParamList.clear();
    }
}
