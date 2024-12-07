package com.rkade;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AxisPanel extends JPanel implements DeviceListener, ActionListener, ChangeListener {
    private final Image target;
    private short xAxisMinimum;
    private short xAxisMaximum;
    private short yAxisMinimum;
    private short yAxisMaximum;
    private int x = 0, y = 0;

    public AxisPanel() {
        target = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("target.png"));
    }

    public void setXAxisMinimum(short xAxisMinimum) {
        this.xAxisMinimum = xAxisMinimum;
    }

    public void setXAxisMaximum(short xAxisMaximum) {
        this.xAxisMaximum = xAxisMaximum;
    }

    public void setYAxisMinimum(short yAxisMinimum) {
        this.yAxisMinimum = yAxisMinimum;
    }

    public void setYAxisMaximum(short yAxisMaximum) {
        this.yAxisMaximum = yAxisMaximum;
    }

    private void setAxisValues(int x, int y) {
        this.x = x;
        this.y = y;
        repaint();
    }

    private void updateControls(AxisDataReport axisDataReport) {
        setAxisValues(axisDataReport.getX(), axisDataReport.getY());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight();
        int w = getWidth();
        if (xAxisMaximum > xAxisMinimum) {
            int nx = normalize(x, xAxisMinimum, xAxisMaximum, 0, w - target.getWidth(null));
            int ny = normalize(y, yAxisMinimum, yAxisMaximum, 0, h - target.getHeight(null));
            g.drawImage(target, nx, ny, null);

            nx = normalize(x, Short.MIN_VALUE, Short.MAX_VALUE, 0, 1023);
            ny = normalize(y, Short.MIN_VALUE, Short.MAX_VALUE, 0, 1023);
            System.out.println(nx + ":" + ny);
        } else if (target.getWidth(null) > 0) {
            g.drawImage(target, w / 2, h / 2, this);
        } else {
            repaint();
        }
    }

    private int map(int value, int in_min, int in_max, int out_min, int out_max) {
        return (int) Math.round(((double) value - in_min) * (double) (out_max - out_min) / (double) (in_max - in_min) + out_min);
    }

    private int normalize(int value, int physicalMinimum, int physicalMaximum, int logicalMinimum, int logicalMaximum) {
        int realMinimum = Math.min(physicalMinimum, physicalMaximum);
        int realMaximum = Math.max(physicalMinimum, physicalMaximum);

        if (value < realMinimum) {
            value = realMinimum;
        }
        if (value > realMaximum) {
            value = realMaximum;
        }

        if (physicalMinimum > physicalMaximum) {
            // Values go from a larger number to a smaller number (e.g. 1024 to 0)
            value = realMaximum - value + realMinimum;
        }
        return map(value, realMinimum, realMaximum, logicalMinimum, logicalMaximum);
    }

    @Override
    public void deviceAttached(Device device) {

    }

    @Override
    public void deviceDetached(Device device) {

    }

    @Override
    public void deviceUpdated(Device device, String status, DataReport report) {
        if (report instanceof AxisDataReport axisDataReport) {
            updateControls(axisDataReport);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }
}
