package br.univille;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Form extends JFrame {
    
    private Producer producer;
    private Consumer consumer;
    private DrawingPanel drawingPanel;
    private Color currentColor = new Color((int)(Math.random() * 0x1000000));
    
    public Form(String titulo, Producer producer, Consumer consumer) {
        setTitle(titulo);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Criar o painel de desenho
        drawingPanel = new DrawingPanel();
        add(drawingPanel);
        
        this.producer = producer;
        this.consumer = consumer;
        setVisible(true);

        
        Timer timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obter os eventos do consumidor e desenhar os pontos
                List<Event> events = consumer.getEvents();
                if(events.size() > 0){
                    for (Event event : events) {
                        drawingPanel.getPoints().add(new Point(event.x(), event.y()));
                    }
                    drawingPanel.repaint();
                }
            }
        });
        if(consumer != null) {
            timer.start();
        }
        
    }


    
    // Classe interna para o painel de desenho
    class DrawingPanel extends JPanel {
        private List<Point> points = new ArrayList<>();
        private final int PIXEL_SIZE = 5;
        
        public List<Point> getPoints() {
            return points;
        }

        public DrawingPanel() {
            setBackground(Color.WHITE);
            
            // Adicionar o listener de mouse
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Adicionar o ponto à lista
                    points.add(new Point(e.getX(), e.getY()));
                    if(producer != null) {
                        Event event = new Event(e.getX(), e.getY(), currentColor.getRGB());
                        producer.publishEvent(event);
                    }
                    repaint();
                }
            });
            
            // Para desenhar ao arrastar o mouse também (opcional)
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    points.add(new Point(e.getX(), e.getY()));
                    if(producer != null) {
                        Event event = new Event(e.getX(), e.getY(), currentColor.getRGB());
                        producer.publishEvent(event);
                    }
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // defina a cor de forma randomica
            g2d.setColor(currentColor);
            
            
            // Desenhar todos os pixels
            for (Point p : points) {
                g2d.fillRect(p.x - (PIXEL_SIZE/2), p.y - (PIXEL_SIZE/2), PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }
    
}