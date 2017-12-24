import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


public class DoubleFilter extends DocumentFilter{
	private int maxChar = 30;
	
	@Override
	 public void replace(FilterBypass fb, int offs, int length,
             String str, AttributeSet a) throws BadLocationException {

         String text = fb.getDocument().getText(0,
                 fb.getDocument().getLength());
         text += str;
         if ((fb.getDocument().getLength() + str.length() - length) <= maxChar
                 && text.matches("[0-9]*\\.?[0-9]*")) {
             super.replace(fb, offs, length, str, a);
         } else {
             Toolkit.getDefaultToolkit().beep();
         }
     }

	@Override
     public void insertString(FilterBypass fb, int offs, String str,
             AttributeSet a) throws BadLocationException {

         String text = fb.getDocument().getText(0,
                 fb.getDocument().getLength());
         text += str;
         if ((fb.getDocument().getLength() + str.length()) <= maxChar
                 && text.matches("[0-9]*\\.?[0-9]*")) {
             super.insertString(fb, offs, str, a);
         } else {
             Toolkit.getDefaultToolkit().beep();
         }
     }
}
