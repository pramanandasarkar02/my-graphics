import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.util.ArrayList;

public class DrawingEditor extends Frame {
    private ArrayList<Point> points = new ArrayList<>(); // Store drawing points
    private DrawingCanvas canvas;

    public DrawingEditor() {
        super("Simple Drawing Editor");
        setSize(400, 400);
        setLayout(new BorderLayout());

        // Canvas for drawing
        canvas = new DrawingCanvas();
        add(canvas, BorderLayout.CENTER);

        // Buttons
        Button saveButton = new Button("Save to XML");
        Button loadButton = new Button("Load from XML");
        Panel buttonPanel = new Panel();
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Mouse listener for drawing
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                points.add(new Point(e.getX(), e.getY()));
                canvas.repaint();
            }
        });

        // Save button action
        saveButton.addActionListener(e -> saveToXML());

        // Load button action
        loadButton.addActionListener(e -> loadFromXML());

        // Window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    // Inner class for the drawing canvas
    class DrawingCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            for (Point p : points) {
                g.fillOval(p.x - 2, p.y - 2, 4, 4); // Draw small dots
            }
        }
    }

    // Save points to XML
    private void saveToXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("drawing");
            doc.appendChild(root);

            for (Point p : points) {
                Element point = doc.createElement("point");
                point.setAttribute("x", String.valueOf(p.x));
                point.setAttribute("y", String.valueOf(p.y));
                root.appendChild(point);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("drawing.xml"));
            transformer.transform(source, result);

            System.out.println("Saved to drawing.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load points from XML
    private void loadFromXML() {
        try {
            points.clear(); // Clear current drawing

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("drawing.xml"));
            doc.getDocumentElement().normalize();

            NodeList pointList = doc.getElementsByTagName("point");
            for (int i = 0; i < pointList.getLength(); i++) {
                Node node = pointList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int x = Integer.parseInt(element.getAttribute("x"));
                    int y = Integer.parseInt(element.getAttribute("y"));
                    points.add(new Point(x, y));
                }
            }

            canvas.repaint();
            System.out.println("Loaded from drawing.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DrawingEditor();
    }
}