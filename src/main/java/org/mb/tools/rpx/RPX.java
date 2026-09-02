package org.mb.tools.rpx;

import org.mb.tools.rpx.ui.RPXGUI;

import javax.swing.*;

public class RPX {

    public static void main(String[] args) {
        // Swing components must be created and shown on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            RPXGUI gui = new RPXGUI();
            gui.setVisible(true);
        });
    }
}
