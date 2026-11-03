package io.github.marcobelligoli.rpx.ui;

import io.github.marcobelligoli.rpx.model.RekordboxPlaylistParam;
import io.github.marcobelligoli.rpx.service.export.ExportServiceFactory;
import io.github.marcobelligoli.rpx.utils.OsUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application GUI
 */
public class RPXGUI extends JFrame {

    private static final int INITIAL_WIDTH = 500;
    private static final int INITIAL_HEIGHT = 200;
    private static final int PATH_FIELD_COLUMNS = 40;
    private static final int MAX_VISIBLE_PLAYLISTS = 8;

    private final JPanel panel;
    private List<RekordboxPlaylistParam> rekordboxPlaylistParamList;
    private String outputFolderPath;

    public RPXGUI() {
        rekordboxPlaylistParamList = new ArrayList<>();
        outputFolderPath = OsUtils.getDesktopPath();
        setTitle("RPX - Rekordbox Playlist Exporter");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setLocationRelativeTo(null);

        panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel);
        initPanel();
    }

    private void initPanel() {
        JButton selectFilesButton = new JButton("SELECT ALL PLAYLIST FILES TO EXPORT");
        selectFilesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            List<String> supportedFormats = ExportServiceFactory.getSupportedFormats();
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Rekordbox playlists (" + String.join(", ", supportedFormats) + ")",
                    supportedFormats.toArray(new String[0])));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                reloadPanel(selectedFiles);
            }
        });

        // a GridBagLayout holding a single component keeps it centred
        JPanel buttonHolder = new JPanel(new GridBagLayout());
        buttonHolder.add(selectFilesButton);
        panel.add(buttonHolder, BorderLayout.CENTER);
    }

    private void reloadPanel(File[] selectedFiles) {
        panel.removeAll();

        JTextField[] filePathFields = new JTextField[selectedFiles.length];
        JCheckBox[] checkBoxes = new JCheckBox[selectedFiles.length];

        panel.add(buildPlaylistsSection(selectedFiles, filePathFields, checkBoxes), BorderLayout.CENTER);
        panel.add(buildFooter(selectedFiles, filePathFields, checkBoxes), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Builds the scrollable list of the selected playlists, one row per file
     */
    private JComponent buildPlaylistsSection(File[] selectedFiles, JTextField[] filePathFields,
                                             JCheckBox[] checkBoxes) {
        JPanel listPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // the checkbox column is labelled once, instead of repeating the text on every row
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        listPanel.add(new JLabel("Keep order"), gbc);

        for (int i = 0; i < selectedFiles.length; i++) {
            String filePath = selectedFiles[i].getAbsolutePath();

            JTextField filePathField = new JTextField(filePath, PATH_FIELD_COLUMNS);
            filePathField.setEditable(false);
            filePathField.setToolTipText(filePath);
            filePathField.setCaretPosition(0);
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            listPanel.add(filePathField, gbc);
            filePathFields[i] = filePathField;

            JCheckBox checkBox = new JCheckBox();
            checkBox.setToolTipText("Prefix every file with its position in the playlist");
            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            listPanel.add(checkBox, gbc);
            checkBoxes[i] = checkBox;
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Playlists to export"));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        limitVisibleRows(scrollPane, listPanel, selectedFiles.length);
        return scrollPane;
    }

    /**
     * Caps the height of the playlist list: beyond MAX_VISIBLE_PLAYLISTS rows the list scrolls,
     * so that selecting many files cannot push the buttons off the screen
     */
    private static void limitVisibleRows(JScrollPane scrollPane, JPanel listPanel, int playlistCount) {
        if (playlistCount <= MAX_VISIBLE_PLAYLISTS) {
            return;
        }
        // every row has the same height, so the visible height can be derived from the whole list
        int totalRows = playlistCount + 1;
        int visibleRows = MAX_VISIBLE_PLAYLISTS + 1;
        Dimension preferredSize = listPanel.getPreferredSize();
        scrollPane.getViewport().setPreferredSize(
                new Dimension(preferredSize.width, preferredSize.height * visibleRows / totalRows));
    }

    /**
     * Builds the bottom part of the screen: the export folder chooser and the action buttons
     */
    private JPanel buildFooter(File[] selectedFiles, JTextField[] filePathFields, JCheckBox[] checkBoxes) {
        JTextField outputFolderField = new JTextField(outputFolderPath, PATH_FIELD_COLUMNS);
        outputFolderField.setEditable(false);
        outputFolderField.setToolTipText(outputFolderPath);
        outputFolderField.setCaretPosition(0);

        JButton changeOutputFolderButton = new JButton("CHANGE...");
        changeOutputFolderButton.addActionListener(e -> chooseOutputFolder(outputFolderField));

        JPanel outputFolderPanel = new JPanel(new BorderLayout(5, 0));
        // same padding the playlist rows get from their insets, so the two sections look alike
        outputFolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Export folder"),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        outputFolderPanel.add(outputFolderField, BorderLayout.CENTER);
        outputFolderPanel.add(changeOutputFolderButton, BorderLayout.EAST);

        JButton backButton = new JButton("BACK");
        backButton.addActionListener(e -> resetPanel());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonBar.add(backButton);
        buttonBar.add(getExportButton(selectedFiles, filePathFields, checkBoxes));

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.add(outputFolderPanel, BorderLayout.NORTH);
        footer.add(buttonBar, BorderLayout.SOUTH);
        return footer;
    }

    private void chooseOutputFolder(JTextField outputFolderField) {
        JFileChooser folderChooser = new JFileChooser(outputFolderPath);
        folderChooser.setDialogTitle("Select the folder where the playlist folders will be created");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = folderChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            outputFolderPath = folderChooser.getSelectedFile().getAbsolutePath();
            outputFolderField.setText(outputFolderPath);
            outputFolderField.setToolTipText(outputFolderPath);
            outputFolderField.setCaretPosition(0);
        }
    }

    private JButton getExportButton(File[] selectedFiles, JTextField[] filePathFields, JCheckBox[] checkBoxes) {
        JButton exportButton = new JButton("EXPORT SELECTED PLAYLIST");
        exportButton.addActionListener(e -> {
            // rebuild the list from scratch, so that retrying after a failed export does not export twice
            rekordboxPlaylistParamList.clear();
            for (int i = 0; i < selectedFiles.length; i++) {
                RekordboxPlaylistParam param = new RekordboxPlaylistParam();
                param.setPlaylistPath(filePathFields[i].getText());
                param.setMaintainPlaylistOrder(checkBoxes[i].isSelected());
                rekordboxPlaylistParamList.add(param);
            }
            try {
                exportPlaylists();
                JOptionPane.showMessageDialog(null, "Operation successfully done!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                resetPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "An error occurred during operation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return exportButton;
    }

    /**
     * Exports the selected playlists, grouped by format so that each group is read by its own service:
     * playlists of different formats can be selected and exported together
     */
    private void exportPlaylists() {
        Map<String, List<RekordboxPlaylistParam>> playlistsByFormat = new LinkedHashMap<>();
        for (RekordboxPlaylistParam param : rekordboxPlaylistParamList) {
            String inputFormat = ExportServiceFactory.getInputFormat(param.getPlaylistPath());
            playlistsByFormat.computeIfAbsent(inputFormat, format -> new ArrayList<>()).add(param);
        }

        for (Map.Entry<String, List<RekordboxPlaylistParam>> playlists : playlistsByFormat.entrySet()) {
            ExportServiceFactory.getExportService(playlists.getKey())
                    .exportPlaylists(playlists.getValue(), outputFolderPath);
        }
    }

    private void resetPanel() {
        panel.removeAll();
        initPanel();
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        rekordboxPlaylistParamList.clear();
    }
}
