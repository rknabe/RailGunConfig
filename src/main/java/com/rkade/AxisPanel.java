package com.rkade;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AxisPanel extends JPanel implements DeviceListener, ActionListener, ChangeListener {
    private final Image target;
    private short xAxisMinimum = Short.MIN_VALUE;
    private short xAxisMaximum = Short.MAX_VALUE;
    private short yAxisMinimum = Short.MIN_VALUE;
    private short yAxisMaximum = Short.MAX_VALUE;
    private short x = 0, y = 0;
    private boolean isCalibrating = false;
    private short xMax = Short.MAX_VALUE;
    private short xMin = Short.MIN_VALUE;
    private short yMax = Short.MAX_VALUE;
    private short yMin = Short.MIN_VALUE;
    private int targetWidth = 0;
    private int targetHeight = 0;

    public AxisPanel() {
        target = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("crosshair.png"));
    }

    private void setAxisValues(short x, short y) {
        this.x = x;
        this.y = y;
        if (isCalibrating) {
            xMax = (short) Math.max(x, xMax);
            xMin = (short) Math.min(x, xMin);
            yMax = (short) Math.max(y, yMax);
            yMin = (short) Math.min(y, yMin);
        }
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
        if (targetWidth == 0 || targetHeight == 0) {
            targetWidth = target.getWidth(null);
            targetHeight = target.getHeight(null);
        }
        int nx = normalize(x, xAxisMinimum, xAxisMaximum, -targetWidth / 2, w - targetWidth / 2);
        int ny = normalize(y, yAxisMinimum, yAxisMaximum, -targetWidth / 2, h - targetHeight / 2);
        g.drawImage(target, nx, ny, null);
    }

    private int map(int value, int in_min, int in_max, int out_min, int out_max) {
        return (int) Math.round(((double) value - in_min) * (double) (out_max - out_min) / (double) (in_max - in_min) + out_min);
    }

    public int normalize(int value, int physicalMinimum, int physicalMaximum, int logicalMinimum, int logicalMaximum) {
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
    public void deviceFound(Device device) {

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

    public void setCalibrating(boolean calibrating) {
        isCalibrating = calibrating;
        if (isCalibrating) {
            xMax = Short.MIN_VALUE;
            xMin = Short.MAX_VALUE;
            yMax = Short.MIN_VALUE;
            yMin = Short.MAX_VALUE;
        } else {
            xAxisMinimum = xMin;
            xAxisMaximum = xMax;
            yAxisMinimum = yMin;
            yAxisMaximum = yMax;
        }
    }

    public short getXAxisMinimum() {
        return xAxisMinimum;
    }

    public void setXAxisMinimum(short xAxisMinimum) {
        this.xAxisMinimum = xAxisMinimum;
    }

    public short getXAxisMaximum() {
        return xAxisMaximum;
    }

    public void setXAxisMaximum(short xAxisMaximum) {
        this.xAxisMaximum = xAxisMaximum;
    }

    public short getYAxisMinimum() {
        return yAxisMinimum;
    }

    public void setYAxisMinimum(short yAxisMinimum) {
        this.yAxisMinimum = yAxisMinimum;
    }

    public short getYAxisMaximum() {
        return yAxisMaximum;
    }

    public void setYAxisMaximum(short yAxisMaximum) {
        this.yAxisMaximum = yAxisMaximum;
    }
}
