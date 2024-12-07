package com.rkade;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class BaseForm implements ActionListener, ChangeListener, FocusListener {
    protected List<JComponent> controls;

    protected void setupControlListener() {
        if (controls != null) {
            for (JComponent component : controls) {
                component.addFocusListener(this);
                switch (component) {
                    case AbstractButton button -> button.addActionListener(this);
                    case JTextField textField -> textField.addActionListener(this);
                    case JSlider slider -> slider.addChangeListener(this);
                    case JSpinner spinner -> spinner.addChangeListener(this);
                    case JComboBox<?> comboBox -> comboBox.addActionListener(this);
                    default -> {
                    }
                }
            }
        }
    }

    protected void setPanelEnabled(boolean enable) {
        //for (JComponent component : controls) {
         //   if (component != null) {
         //       component.setEnabled(enable);
         //   }
        //}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    protected final BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(55, 55, BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bufferedImage;
    }

    protected final BufferedImage rotate(BufferedImage image, Double degrees) {
        // Calculate the new size of the image based on the angle of rotation
        double radians = Math.toRadians(degrees);
        int newWidth = image.getWidth();
        int newHeight = image.getHeight();

        // Create a new image
        BufferedImage rotate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotate.createGraphics();
        // Calculate the "anchor" point around which the image will be rotated
        int x = (newWidth - image.getWidth()) / 2;
        int y = (newHeight - image.getHeight()) / 2;
        // Transform the origin point around the anchor point
        AffineTransform at = new AffineTransform();
        at.setToRotation(radians, x + (image.getWidth() / 2.0), y + (image.getHeight() / 2.0));
        at.translate(x, y);
        g2d.setTransform(at);
        // Paint the original image
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rotate;
    }
}
