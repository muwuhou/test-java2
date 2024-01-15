import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
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

    JPanel bottomPane = new JPanel();
    // Default LayoutManager for JPanel is FlowLayoutManager
    JLabel label = new JLabel("Enter Name");
    JTextField textField = new JTextField(16);
    JButton button = new JButton("Submit");
    bottomPane.add(label);
    bottomPane.add(textField);
    bottomPane.add(button);
    bottomPane.setBorder(BorderFactory.createTitledBorder("hello"));

    JPanel messagePane = new JPanel(new BorderLayout());
    messagePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JTextArea textArea = new JTextArea();
    textArea.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 1));
    messagePane.add(textArea, BorderLayout.CENTER);

    MyDrawing myDrawing = new MyDrawing();
    // Default LayoutManager for content pane is BorderLayoutManager
    frame.getContentPane().add(messagePane, BorderLayout.CENTER);
    frame.getContentPane().add(myDrawing, BorderLayout.LINE_END);
    frame.getContentPane().add(bottomPane, BorderLayout.PAGE_END);

    button.addActionListener(e -> textArea.append(textField.getText() + "\n"));

    frame.setVisible(true);
  }

  // A custom widget that do our own painting
  private static class MyDrawing extends JPanel {
    private final Random _random = new Random();

    MyDrawing() {
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          repaint();
        }
      });
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(200, 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      int hi = getSize().height;
      int wi = getSize().width;
      g.setColor(Color.BLUE);
      g.drawRect(5, 5, wi - 10, hi - 10);

      for (int i = 0; i < 10; ++i) {
        int x = 5 + _random.nextInt(wi - 10);
        int y = 5 + _random.nextInt(hi - 10);
        if (i % 2 == 0) {
          g.setColor(Color.BLACK);
          g.fillRect(x, y, 10, 10);
        } else {
          g.setColor(Color.RED);
          g.fillOval(x, y, 10, 10);
        }
      }
    }
  }
}
