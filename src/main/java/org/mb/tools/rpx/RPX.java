package org.mb.tools.rpx;

import org.mb.tools.rpx.ui.RPXGUI;
import org.mb.tools.rpx.utils.LogUtils;

import javax.swing.*;
import java.util.logging.Logger;

public class RPX {

    private static final Logger logger = Logger.getLogger(RPX.class.getName());

    public static void main(String[] args) {
        // Swing components must be created and shown on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            RPXGUI gui = new RPXGUI();
            gui.setVisible(true);
        });
    }

    /**
     * Applies the look and feel of the host operating system, so that the window and the file chooser
     * look native on Windows, macOS and Linux. Falls back to the default one when it is not available.
     */
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            LogUtils.error(logger, "Impossible to set the system look and feel: " + e.getMessage());
        }
    }
}
