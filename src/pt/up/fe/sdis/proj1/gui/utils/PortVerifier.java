package pt.up.fe.sdis.proj1.gui.utils;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import pt.up.fe.sdis.proj1.utils.NetworkUtils;

public class PortVerifier extends InputVerifier {

    public PortVerifier() {
    }
    
    public PortVerifier(JLabel errLabel) {
        _errorLabel = errLabel;
    }
    private JLabel _errorLabel = null;
    private Border _prevBorder = null;
    
    @Override
    public boolean shouldYieldFocus(JComponent input) {
        if (verify(input)) {
            if (_errorLabel != null) _errorLabel.setVisible(false);
            if (_prevBorder != null) input.setBorder(_prevBorder);
        }
        else {
            if (_errorLabel != null) _errorLabel.setVisible(true);
            Toolkit.getDefaultToolkit().beep();
            _prevBorder = input.getBorder();
            input.setBorder(BorderFactory.createEtchedBorder(Color.red, Color.red));
        }
        return true;
    }
    
    @Override
    public boolean verify(JComponent input) {
        if (!(input instanceof JTextComponent))
            return false;
        String inputStr = ((JTextComponent) input).getText();
        return NetworkUtils.isValidPort(inputStr);
    }

}
