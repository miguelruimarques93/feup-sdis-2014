package pt.up.fe.sdis.proj1.gui.utils;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class TextFieldWithPlaceholder extends JTextField implements KeyListener, FocusListener {

    @Override
    public void setText(String arg0) {
        if (arg0 == null || arg0.equals("")) {
            super.setText(_placeholderStr);
            super.setForeground(_placeholderColor);
        } else {
            super.setText(arg0);
            super.setForeground(_defaultColor);
        }
        
    }

    private static final long serialVersionUID = 1L;
    
    public TextFieldWithPlaceholder( String placeholderStr, Color placeholderColor) {
        super(placeholderStr);
        
        addKeyListener(this);
        addFocusListener(this);
        
        _placeholderColor = placeholderColor;
        _placeholderStr = placeholderStr;
        _defaultColor = getForeground();
        
        super.setForeground(_placeholderColor);
    }

    @Override
    public void setForeground(Color arg0) {
        _defaultColor = arg0;
        super.setForeground(arg0);
    }
    
    @Override
    public void focusGained(FocusEvent arg0) {
        selectAll();
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        if (getText().isEmpty()) {
            super.setForeground(_placeholderColor);
            setText(_placeholderStr);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) {
        super.setForeground(_defaultColor);
    }

    private Color _placeholderColor;
    private Color _defaultColor;
    private String _placeholderStr;
    
}
