package pt.up.fe.sdis.proj1.gui.utils;

import javax.swing.UIManager;

public class GuiUtils {
    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
    }
}
