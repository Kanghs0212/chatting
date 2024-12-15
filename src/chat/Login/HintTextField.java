package chat.Login;

import javax.swing.*;
import java.awt.*;

public class HintTextField extends JTextField {
    private String hint;

    public HintTextField(String hint, int col){
        this.hint = hint;
        this.setColumns(col);
    }

    @Override
    protected  void paintComponent(Graphics g){
        super.paintComponent(g);
        if(getText().isEmpty()){
            g.setColor(Color.GRAY);
            g.drawString(hint,getInsets().left, g.getFontMetrics().getAscent()+getInsets().top);
        }
    }
}