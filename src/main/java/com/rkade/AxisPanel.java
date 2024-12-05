package com.rkade;

import javax.swing.*;
import java.awt.*;

public class AxisPanel extends JPanel {
    private int x = 0, y = 0;

    public void setAxisValues(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the cursor
        g.setColor(Color.BLACK);
        g.fillOval(x, y, 10, 10);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
