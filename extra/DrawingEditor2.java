import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.util.ArrayList;

public class DrawingEditor2 extends Frame {
    private ArrayList<Shape> shapes = new ArrayList<>(); // Store shapes
    private DrawingCanvas canvas;
    private String currentShape = "dot"; // Default shape
    private Point startPoint; // For drawing lines and rectangles

    public DrawingEditor2() {
        super("Enhanced Drawing Editor");
        setSize(400, 400);
        setLayout(new BorderLayout());

        // Canvas for drawing
        canvas = new DrawingCanvas();
        add(canvas, BorderLayout.CENTER);

        // Shape selection
        Panel shapePanel = new Panel();
        CheckboxGroup shapeGroup = new CheckboxGroup();
        Checkbox dot = new Checkbox("Dot", shapeGroup, true);
        Checkbox line = new Checkbox("Line", shapeGroup, false);
        Checkbox rect = new Checkbox("Rectangle", shapeGroup, false);
        shapePanel.add(dot);
        shapePanel.add(line);
        shapePanel.add(rect);
        add(shapePanel, BorderLayout.NORTH);

        // Buttons
        Button saveAsButton = new Button("Save As");
        Button loadButton = new Button("Load");
        Panel buttonPanel = new Panel();
        buttonPanel.add(saveAsButton);
        buttonPanel.add(loadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Shape selection logic
        dot.addItemListener(e -> currentShape = "dot");
        line.addItemListener(e -> currentShape = "line");
        rect.addItemListener(e -> currentShape = "rect");

        // Mouse listeners for drawing
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = new Point(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point endPoint = new Point(e.getX(), e.getY());
                switch (currentShape) {
                    case "dot":
                        shapes.add(new Dot(endPoint.x, endPoint.y));
                        break;
                    case "line":
                        shapes.add(new Line(startPoint.x, startPoint.y, endPoint.x, endPoint.y));
                        break;
                    case "rect":
                        int width = Math.abs(endPoint.x - startPoint.x);
                        int height = Math.abs(endPoint.y - startPoint.y);
                        int x = Math.min(startPoint.x, endPoint.x);
                        int y = Math.min(startPoint.y, endPoint.y);
                        shapes.add(new Rectangle(x, y, width, height));
                        break;
                }
                canvas.repaint();
            }
        });

        // Save As button action
        saveAsButton.addActionListener(e -> saveAsXML());

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
            for (Shape shape : shapes) {
                shape.draw(g);
            }
        }
    }

    // Shape interface
    interface Shape {
        void draw(Graphics g);
    }

    // Dot class
    class Dot implements Shape {
        int x, y;
        Dot(int x, int y) { this.x = x; this.y = y; }
        @Override
        public void draw(Graphics g) { g.fillOval(x - 2, y - 2, 4, 4); }
    }

    // Line class
    class Line implements Shape {
        int x1, y1, x2, y2;
        Line(int x1, int y1, int x2, int y2) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; }
        @Override
        public void draw(Graphics g) { g.drawLine(x1, y1, x2, y2); }
    }

    // Rectangle class
    class Rectangle implements Shape {
        int x, y, width, height;
        Rectangle(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; }
        @Override
        public void draw(Graphics g) { g.drawRect(x, y, width, height); }
    }

    // Save As to .pxml
    private void saveAsXML() {
        FileDialog fd = new FileDialog(this, "Save As", FileDialog.SAVE);
        fd.setFile("*.pxml"); // Suggest .pxml extension
        fd.setVisible(true);
        String filename = fd.getDirectory() + fd.getFile();
        if (filename != null) {
            if (!filename.endsWith(".pxml")) filename += ".pxml";
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.newDocument();

                Element root = doc.createElement("drawing");
                doc.appendChild(root);

                for (Shape shape : shapes) {
                    Element shapeEl;
                    if (shape instanceof Dot) {
                        Dot dot = (Dot) shape;
                        shapeEl = doc.createElement("dot");
                        shapeEl.setAttribute("x", String.valueOf(dot.x));
                        shapeEl.setAttribute("y", String.valueOf(dot.y));
                    } else if (shape instanceof Line) {
                        Line line = (Line) shape;
                        shapeEl = doc.createElement("line");
                        shapeEl.setAttribute("x1", String.valueOf(line.x1));
                        shapeEl.setAttribute("y1", String.valueOf(line.y1));
                        shapeEl.setAttribute("x2", String.valueOf(line.x2));
                        shapeEl.setAttribute("y2", String.valueOf(line.y2));
                    } else { // Rectangle
                        Rectangle rect = (Rectangle) shape;
                        shapeEl = doc.createElement("rect");
                        shapeEl.setAttribute("x", String.valueOf(rect.x));
                        shapeEl.setAttribute("y", String.valueOf(rect.y));
                        shapeEl.setAttribute("width", String.valueOf(rect.width));
                        shapeEl.setAttribute("height", String.valueOf(rect.height));
                    }
                    root.appendChild(shapeEl);
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(filename));
                transformer.transform(source, result);

                System.out.println("Saved to " + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Load from .pxml
    private void loadFromXML() {
        FileDialog fd = new FileDialog(this, "Load", FileDialog.LOAD);
        fd.setFile("*.pxml"); // Filter for .pxml files
        fd.setVisible(true);
        String filename = fd.getDirectory() + fd.getFile();
        if (filename != null) {
            try {
                shapes.clear(); // Clear current drawing

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new File(filename));
                doc.getDocumentElement().normalize();

                NodeList shapeList = doc.getElementsByTagName("*");
                for (int i = 0; i < shapeList.getLength(); i++) {
                    Node node = shapeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        switch (element.getTagName()) {
                            case "dot":
                                int x = Integer.parseInt(element.getAttribute("x"));
                                int y = Integer.parseInt(element.getAttribute("y"));
                                shapes.add(new Dot(x, y));
                                break;
                            case "line":
                                int x1 = Integer.parseInt(element.getAttribute("x1"));
                                int y1 = Integer.parseInt(element.getAttribute("y1"));
                                int x2 = Integer.parseInt(element.getAttribute("x2"));
                                int y2 = Integer.parseInt(element.getAttribute("y2"));
                                shapes.add(new Line(x1, y1, x2, y2));
                                break;
                            case "rect":
                                int rx = Integer.parseInt(element.getAttribute("x"));
                                int ry = Integer.parseInt(element.getAttribute("y"));
                                int width = Integer.parseInt(element.getAttribute("width"));
                                int height = Integer.parseInt(element.getAttribute("height"));
                                shapes.add(new Rectangle(rx, ry, width, height));
                                break;
                        }
                    }
                }

                canvas.repaint();
                System.out.println("Loaded from " + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new DrawingEditor2();
    }
}