import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Puzzle {
    public static void main(String[] args) {

        //set the look and feel to system's look and feel, who likes java's native look and feel :"D
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        SoundManager soundManager = new SoundManager();
        soundManager.playSound("background.wav");

        //start the gui, let the magic begin :D
        GUI gui = new GUI();
        gui.setVisible(true);

    }

}
