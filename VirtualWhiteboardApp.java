import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VirtualWhiteboardApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Show login screen first+
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }

    // Login Page Class
    static class LoginPage extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginPage() {
            setTitle("Login");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); // Center the login window

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(10, 10));

            JLabel usernameLabel = new JLabel("Username:");
            JLabel passwordLabel = new JLabel("Password:");

            usernameField = new JTextField();
            passwordField = new JPasswordField();

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> handleLogin());

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);
            panel.add(loginButton);

            getContentPane().add(panel);
        }

        private void handleLogin() {
            // Hardcoded username and password for simplicity
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.equals("admin") && password.equals("password")) {
                // Successful login, open the whiteboard
                setVisible(false);
                VirtualWhiteboard whiteboard = new VirtualWhiteboard();
                whiteboard.setVisible(true);
            } else {
                // Invalid login
                JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Virtual Whiteboard Class
    static class VirtualWhiteboard extends JFrame {
        private DrawingPanel drawingPanel;
        private JToolBar toolbar;
        private Color currentColor = Color.BLACK;
        private String currentTool = "FREEHAND";
        private int strokeWidth = 2;
        private boolean fillShape = false;

        public VirtualWhiteboard() {
            setTitle("Virtual Whiteboard");
            setSize(1500, 1000);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create drawing panel
            drawingPanel = new DrawingPanel();

            // Create toolbar
            createToolbar();

            // Add components to frame
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(toolbar, BorderLayout.NORTH);
            getContentPane().add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
        }

        private void createToolbar() {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);

            // Tool Selection Buttons
            JButton freehandButton = createStyledButton("Freehand", "FREEHAND");
            JButton rectangleButton = createStyledButton("Rectangle", "RECTANGLE");
            JButton circleButton = createStyledButton("Circle", "CIRCLE");
            JButton lineButton = createStyledButton("Line", "LINE");
            JButton triangleButton = createStyledButton("Triangle", "TRIANGLE");
            JButton eraserButton = createStyledButton("Eraser", "ERASER");
            JButton textButton = createStyledButton("Text", "TEXT");

            // Color Picker Button
            JButton colorButton = new JButton("Color");
            styleButton(colorButton);
            colorButton.addActionListener(e -> {
                Color selectedColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
                if (selectedColor != null) {
                    currentColor = selectedColor;
                }
            });

            // Stroke Width Slider
            JSlider strokeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
            strokeSlider.setMaximumSize(new Dimension(200, 40));
            strokeSlider.addChangeListener(e -> strokeWidth = strokeSlider.getValue());

            // Fill Shape Checkbox
            JCheckBox fillCheckbox = new JCheckBox("Fill Shape");
            fillCheckbox.setFont(new Font("Arial", Font.BOLD, 16));
            fillCheckbox.addActionListener(e -> fillShape = fillCheckbox.isSelected());

            // Save Button
            JButton saveButton = new JButton("Save");
            styleButton(saveButton);
            saveButton.addActionListener(e -> saveDrawing());

            // Clear Button
            JButton clearButton = new JButton("Clear");
            styleButton(clearButton);
            clearButton.addActionListener(e -> drawingPanel.clear());

            // Add components to toolbar
            toolbar.add(freehandButton);
            toolbar.add(rectangleButton);
            toolbar.add(circleButton);
            toolbar.add(lineButton);
            toolbar.add(triangleButton);
            toolbar.add(eraserButton);
            toolbar.add(textButton);
            toolbar.addSeparator();
            toolbar.add(colorButton);
            toolbar.add(strokeSlider);
            toolbar.add(fillCheckbox);
            toolbar.addSeparator();
            toolbar.add(saveButton);
            toolbar.add(clearButton);
        }

        private JButton createStyledButton(String name, String tool) {
            JButton button = new JButton(name);
            styleButton(button);
            button.addActionListener(e -> currentTool = tool);
            return button;
        }

        private void styleButton(JButton button) {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setPreferredSize(new Dimension(100, 60));
            button.setMargin(new Insets(10, 15, 10, 15)); // Add some padding
        }

        private void saveDrawing() {
            try {
                BufferedImage image = new BufferedImage(
                    drawingPanel.getWidth(),
                    drawingPanel.getHeight(),
                    BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g2d = image.createGraphics();
                drawingPanel.paint(g2d);
                g2d.dispose();

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Whiteboard");
                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String filePath = fileToSave.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".png")) {
                        fileToSave = new File(filePath + ".png");
                    }
                    ImageIO.write(image, "png", fileToSave);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
            }
        }

        // Drawing Panel Inner Class
        private class DrawingPanel extends JPanel {
            private BufferedImage image;
            private Point startPoint;

            public DrawingPanel() {
                setBackground(Color.WHITE);

                // Add mouse listeners
                MouseAdapter mouseAdapter = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        startPoint = e.getPoint();

                        if ("TEXT".equals(currentTool)) {
                            String text = JOptionPane.showInputDialog("Enter Text:");
                            if (text != null && !text.trim().isEmpty()) {
                                Graphics2D g2d = (Graphics2D) image.getGraphics();
                                g2d.setColor(currentColor);
                                g2d.setFont(new Font("Arial", Font.PLAIN, 20));
                                g2d.drawString(text, e.getX(), e.getY());
                                repaint();
                            }
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        Graphics2D g2d = (Graphics2D) image.getGraphics();
                        g2d.setColor(currentTool.equals("ERASER") ? Color.WHITE : currentColor);
                        g2d.setStroke(new BasicStroke(strokeWidth));

                        int x = Math.min(startPoint.x, e.getX());
                        int y = Math.min(startPoint.y, e.getY());
                        int width = Math.abs(e.getX() - startPoint.x);
                        int height = Math.abs(e.getY() - startPoint.y);

                        switch (currentTool) {
                            case "RECTANGLE":
                                if (fillShape) g2d.fillRect(x, y, width, height);
                                else g2d.drawRect(x, y, width, height);
                                break;
                            case "CIRCLE":
                                if (fillShape) g2d.fillOval(x, y, width, height);
                                else g2d.drawOval(x, y, width, height);
                                break;
                            case "LINE":
                                g2d.drawLine(startPoint.x, startPoint.y, e.getX(), e.getY());
                                break;
                            case "TRIANGLE":
                                int[] xPoints = {startPoint.x, e.getX(), (startPoint.x + e.getX()) / 2};
                                int[] yPoints = {startPoint.y, e.getY(), Math.min(startPoint.y, e.getY())};
                                if (fillShape) g2d.fillPolygon(xPoints, yPoints, 3);
                                else g2d.drawPolygon(xPoints, yPoints, 3);
                                break;
                        }
                        repaint();
                    }
                };

                addMouseListener(mouseAdapter);
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if ("FREEHAND".equals(currentTool) || "ERASER".equals(currentTool)) {
                            Graphics2D g2d = (Graphics2D) image.getGraphics();
                            g2d.setColor(currentTool.equals("ERASER") ? Color.WHITE : currentColor);
                            g2d.setStroke(new BasicStroke(strokeWidth));
                            g2d.drawLine(startPoint.x, startPoint.y, e.getX(), e.getY());
                            startPoint = e.getPoint();
                            repaint();
                        }
                    }
                });

                // Initialize image
                image = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.dispose();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }

            public void clear() {
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.dispose();
                repaint();
            }
        }
    }
}
