import java.awt.*;
import javax.swing.*;


public class TestSwingApp {

  public static void main(String[] args) {
    TestSwingApp app = new TestSwingApp();
    app.testSwing1();
  }

  void testSwing1() {
    JFrame frame = new JFrame("TestSwingApp");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 300);

    JPanel panel1 = new JPanel();
    JLabel label = new JLabel("Enter Name");
    JTextField textField = new JTextField(16);
    JButton button = new JButton("Submit");
    panel1.add(label);
    panel1.add(textField);
    panel1.add(button);

    JTextArea textArea = new JTextArea();

    frame.getContentPane().add(BorderLayout.SOUTH, panel1);
    frame.getContentPane().add(BorderLayout.CENTER, textArea);
    frame.setVisible(true);
  }
}
