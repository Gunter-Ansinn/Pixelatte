package net.ansinn.pixelatte;

import net.ansinn.pixelatte.output.safe.PixelResource;
import net.ansinn.pixelatte.output.safe.StaticImage8;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;

public class ImagePreviewTest {

    private static final List<String> IMAGE_NAMES = List.of(
            "basn0g01", "basn0g02", "basn0g04", "basn0g08", "basn0g16",
            "basn2c08", "basn2c16",
            "basn3p01", "basn3p02", "basn3p04", "basn3p08",
            "basn4a08", "basn4a16",
            "basn6a08", "basn6a16"
    );

    private int currentIndex = 0;

    @Test
    void previewCompare() throws Exception {
        JFrame frame = new JFrame("Pixelatte vs ImageIO Debugger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JFrame chunkFrame = new JFrame("Chunk Info");
        chunkFrame.setSize(300, 400);
        JTextArea chunkTextArea = new JTextArea();
        chunkTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chunkFrame.add(new JScrollPane(chunkTextArea));
        chunkFrame.setVisible(true);

        // Three panels: Pixelatte, ImageIO, Diff
        JPanel displayPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        JLabel pLabel = createLabel("Pixelatte");
        JLabel ioLabel = createLabel("ImageIO");
        JLabel diffLabel = createLabel("Difference (Magenta = Diff)");

        displayPanel.add(pLabel);
        displayPanel.add(ioLabel);
        displayPanel.add(diffLabel);

        JLabel infoLabel = new JLabel("Click to next...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        frame.add(displayPanel, BorderLayout.CENTER);
        frame.add(infoLabel, BorderLayout.SOUTH);

        // Initial Load
        updateImages(pLabel, ioLabel, diffLabel, infoLabel, chunkTextArea, frame);

        displayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentIndex = (currentIndex + 1) % IMAGE_NAMES.size();
                updateImages(pLabel, ioLabel, diffLabel, infoLabel, chunkTextArea, frame);
            }
        });

        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        chunkFrame.setLocation(frame.getX() + frame.getWidth(), frame.getY());

        System.out.println("Debug view running...");
        Thread.currentThread().join();
    }

    private JLabel createLabel(String title) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setText(title);
        return label;
    }

    private void updateImages(JLabel pLabel, JLabel ioLabel, JLabel diffLabel, JLabel info, JTextArea chunkText, JFrame frame) {
        String name = IMAGE_NAMES.get(currentIndex);
        info.setText("Viewing: " + name + " (" + (currentIndex + 1) + "/" + IMAGE_NAMES.size() + ")");

        mapRes2File("/png_tests/basic_formats/" + name + ".png").ifPresent(file -> {
            try {
                // Load Pixelatte
                PixelResource res = TextureLoader.readFile(file);
                BufferedImage pImg = null;
                if (res != null) {
                    pImg = TestUtils.toBufferedImage(res.asStatic().flattenTo8Bit());
                    String chunks = res.chunkMap().getAllChunks().stream()
                            .map(c -> c.getClass().getSimpleName() + ": " + c.toString())
                            .collect(Collectors.joining("\n"));
                    chunkText.setText(name + "\n\n" + chunks);
                } else {
                    chunkText.setText("Failed to load " + name);
                }

                // Load ImageIO
                BufferedImage ioImg = ImageIO.read(file);

                // Generate Diff
                BufferedImage diffImg = null;
                if (pImg != null && ioImg != null) {
                    diffImg = generateDiff(pImg, ioImg);
                }

                // Set Icons (Scale up for pixel art)
                int scale = 8;
                pLabel.setIcon(pImg != null ? new ImageIcon(scale(pImg, scale)) : null);
                ioLabel.setIcon(ioImg != null ? new ImageIcon(scale(ioImg, scale)) : null);
                diffLabel.setIcon(diffImg != null ? new ImageIcon(scale(diffImg, scale)) : null);

            } catch (Exception e) {
                e.printStackTrace();
                chunkText.setText("Error: " + e.getMessage());
            }
        });
        frame.repaint();
    }

    private BufferedImage generateDiff(BufferedImage a, BufferedImage b) {
        int w = Math.min(a.getWidth(), b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());
        BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pA = a.getRGB(x, y);
                int pB = b.getRGB(x, y);

                int aA = (pA >> 24) & 0xFF;
                int aB = (pB >> 24) & 0xFF;

                // 1. If both are fully transparent, they match regardless of RGB
                if (aA == 0 && aB == 0) {
                    diff.setRGB(x, y, 0xFF000000);
                    continue;
                }

                // 2. Unpack components for threshold comparison
                int rA = (pA >> 16) & 0xFF;
                int gA = (pA >> 8) & 0xFF;
                int bA = pA & 0xFF;

                int rB = (pB >> 16) & 0xFF;
                int gB = (pB >> 8) & 0xFF;
                int bB = pB & 0xFF;

                // Threshold of 2 to allow for rounding errors or slight gamma/conversion differences
                boolean match = Math.abs(aA - aB) <= 2 &&
                                Math.abs(rA - rB) <= 2 &&
                                Math.abs(gA - gB) <= 2 &&
                                Math.abs(bA - bB) <= 2;

                if (!match) {
                    // Difference found: Draw Magenta
                    diff.setRGB(x, y, 0xFFFF00FF);
                } else {
                    // Match: Draw black
                    diff.setRGB(x, y, 0xFF000000); 
                }
            }
        }
        return diff;
    }

    private Image scale(BufferedImage img, int factor) {
        int w = img.getWidth() * factor;
        int h = img.getHeight() * factor;
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }
}