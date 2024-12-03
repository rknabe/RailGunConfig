package com.rkade;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.logging.Logger;

public class AxisPanel extends BaseForm implements DeviceListener, ActionListener, FocusListener {
    private final static Logger logger = Logger.getLogger(AxisPanel.class.getName());
    private JLabel minLabel;
    private JFormattedTextField minText;
    private JLabel centerLabel;
    private JFormattedTextField centerText;
    private JProgressBar progress;
    private JLabel maxLabel;
    private JFormattedTextField maxText;
    private JFormattedTextField dzText;
    private JLabel valueLabel;
    private JTextField valueText;
    private JLabel rawLabel;
    private JTextField rawText;
    private JLabel deadZoneLabel;
    private JPanel mainPanel;
    private JButton setMinButton;
    private JButton setCenterButton;
    private JButton setMaxButton;
    private JCheckBox autoLimitCheckBox;
    private JCheckBox hasCenterCheckBox;
    private JCheckBox enabledCheckBox;
    private JComboBox<String> trimComboBox;
    private JLabel trimLabel;
    private Device device = null;
    private short axisIndex;
    private boolean wasEnabled = false;

    public AxisPanel() {
        IntegerFormatterFactory formatterFactory = new IntegerFormatterFactory(Short.MIN_VALUE, Short.MAX_VALUE);
        minText.setFormatterFactory(formatterFactory);
        maxText.setFormatterFactory(formatterFactory);
        centerText.setFormatterFactory(formatterFactory);
        dzText.setFormatterFactory(formatterFactory);

        controls = List.of(minText, centerText, maxText, dzText, setMinButton, setCenterButton,
                setMaxButton, autoLimitCheckBox, hasCenterCheckBox, enabledCheckBox, trimComboBox);

        setPanelEnabled(false);
        setupControlListener();
    }

    public void setAxisIndex(short axisIndex) {
        this.axisIndex = axisIndex;
    }

    public void setAxisLabel(String axisLabel) {
        TitledBorder border = (TitledBorder) mainPanel.getBorder();
        border.setTitle(axisLabel);
    }

    @Override
    public void deviceAttached(Device device) {
        this.device = device;
        if (wasEnabled) {
            setPanelEnabled(true);
        }
    }

    @Override
    public void deviceDetached(Device device) {
        this.device = null;
        setPanelEnabled(false);
    }

    @Override
    public void deviceUpdated(Device device, String status, DataReport report) {
        if (report != null) {
            if (report.getReportType() == Device.DATA_REPORT_ID) {
                if (report instanceof AxisDataReport axisData) {
                    updateControls(axisData);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean status = handleAction(e);
        if (!status) {
            logger.warning("Action failed for:" + e.getActionCommand());
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        boolean status = handleFocusLost(e);
        if (!status) {
            logger.warning("Focus lost, failed for:" + e.getSource());
        }
    }

    private boolean handleAction(ActionEvent e) {
        if (device != null) {
            if (e.getActionCommand().equals(setMinButton.getActionCommand())) {
                short min = Short.parseShort(rawText.getText());
                short max = Short.parseShort(maxText.getText());
                if (max > min) {
                    return device.setAxisLimits(axisIndex, min, max);
                }
            } else if (e.getActionCommand().equals(setMaxButton.getActionCommand())) {
                short min = Short.parseShort(minText.getText());
                short max = Short.parseShort(rawText.getText());
                if (max > min) {
                    return device.setAxisLimits(axisIndex, min, max);
                }
            } else if (e.getActionCommand().equals(setCenterButton.getActionCommand())) {
                centerText.setText(rawText.getText());
                return device.setAxisCenter(axisIndex, Short.parseShort(rawText.getText()));
            } else if (e.getActionCommand().equals(hasCenterCheckBox.getActionCommand())) {
                if (hasCenterCheckBox.isSelected()) {
                    centerText.setEnabled(true);
                    dzText.setEnabled(true);
                    setCenterButton.setEnabled(true);
                    return device.setAxisCenter(axisIndex, Short.parseShort(centerText.getText()));
                } else {
                    centerText.setEnabled(false);
                    dzText.setEnabled(false);
                    setCenterButton.setEnabled(false);
                    return device.setAxisCenter(axisIndex, Short.MIN_VALUE);
                }
            } else if (e.getActionCommand().equals(autoLimitCheckBox.getActionCommand())) {
                if (autoLimitCheckBox.isSelected()) {
                    return device.setAxisAutoLimit(axisIndex, (short) 1);
                } else {
                    return device.setAxisAutoLimit(axisIndex, (short) 0);
                }
            } else if (e.getActionCommand().equals(enabledCheckBox.getActionCommand())
                    || e.getActionCommand().equals(trimComboBox.getActionCommand())) {
                short disabled = 1;
                if (enabledCheckBox.isSelected()) {
                    disabled = 0;
                }
                if (disabled == 1) {
                    progress.setValue(0);
                }
                //these are inverted, since the original value is isDisabled
                return device.setAxisEnabledAndTrim(axisIndex, disabled, (short) trimComboBox.getSelectedIndex()); //last param is trim index
            }
        }
        return true;
    }

    private boolean handleFocusLost(FocusEvent e) {
        if (device != null) {
            if (e.getSource() == minText || e.getSource() == maxText) {
                short min = Short.parseShort(minText.getText());
                short max = Short.parseShort(maxText.getText());
                if (max > min) {
                    return device.setAxisLimits(axisIndex, min, max);
                }
            } else if (e.getSource() == centerText) {
                return device.setAxisCenter(axisIndex, Short.parseShort(centerText.getText()));
            } else if (e.getSource() == dzText) {
                return device.setAxisDeadZone(axisIndex, Short.parseShort(dzText.getText()));
            }
        }
        return true;
    }

    private void updateControls(AxisDataReport axisData) {
        enabledCheckBox.setSelected(axisData.isEnabled());
        if (wasEnabled && !axisData.isEnabled()) {
            setPanelEnabled(false);
        } else if (!wasEnabled && axisData.isEnabled()) {
            setPanelEnabled(true);
        }
        wasEnabled = axisData.isEnabled();
        if (axisData.isEnabled()) {
            progress.setValue(axisData.getRawValue());
            progress.setMaximum(axisData.getMax());
            progress.setMinimum(axisData.getMin());
            valueText.setText(String.valueOf(axisData.getValue()));
            rawText.setText(String.valueOf(axisData.getRawValue()));

            if (!minText.isFocusOwner()) {
                minText.setText(String.valueOf(axisData.getMin()));
            }
            if (!maxText.isFocusOwner()) {
                maxText.setText(String.valueOf(axisData.getMax()));
            }
            if (!centerText.isFocusOwner()) {
                String value = String.valueOf(axisData.getCenter());
                if (!value.equals(centerText.getText())) {
                    centerText.setText(value);
                }
            }
            if (!dzText.isFocusOwner()) {
                dzText.setText(String.valueOf(axisData.getDeadZone()));
            }
            if (!hasCenterCheckBox.isFocusOwner()) {
                hasCenterCheckBox.setSelected(axisData.isHasCenter());
                centerText.setEnabled(axisData.isHasCenter());
                dzText.setEnabled(axisData.isHasCenter());
                setCenterButton.setEnabled(axisData.isHasCenter());
            }
            if (!autoLimitCheckBox.isFocusOwner()) {
                autoLimitCheckBox.setSelected(axisData.isAutoLimit());
            }
            if (!trimComboBox.isFocusOwner()) {
                trimComboBox.setSelectedIndex(axisData.getTrim());
            }
        }
        //should always be enabled
        enabledCheckBox.setEnabled(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setAlignmentX(1.0f);
        mainPanel.setAlignmentY(1.0f);
        mainPanel.setMaximumSize(new Dimension(32767, 32767));
        mainPanel.setMinimumSize(new Dimension(1024, 110));
        mainPanel.setPreferredSize(new Dimension(1024, 110));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Axis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        minLabel = new JLabel();
        minLabel.setHorizontalAlignment(4);
        minLabel.setMinimumSize(new Dimension(26, 17));
        minLabel.setPreferredSize(new Dimension(26, 17));
        minLabel.setText("Min");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(minLabel, gbc);
        centerLabel = new JLabel();
        centerLabel.setHorizontalAlignment(4);
        centerLabel.setHorizontalTextPosition(2);
        centerLabel.setMinimumSize(new Dimension(63, 17));
        centerLabel.setPreferredSize(new Dimension(63, 17));
        centerLabel.setText("Center");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(centerLabel, gbc);
        centerText = new JFormattedTextField();
        centerText.setHorizontalAlignment(2);
        centerText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(centerText, gbc);
        maxLabel = new JLabel();
        maxLabel.setHorizontalAlignment(4);
        maxLabel.setText("Max");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(maxLabel, gbc);
        minText = new JFormattedTextField();
        minText.setFocusLostBehavior(1);
        minText.setHorizontalAlignment(2);
        minText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(minText, gbc);
        maxText = new JFormattedTextField();
        maxText.setHorizontalAlignment(2);
        maxText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(maxText, gbc);
        deadZoneLabel = new JLabel();
        deadZoneLabel.setHorizontalAlignment(4);
        deadZoneLabel.setHorizontalTextPosition(2);
        deadZoneLabel.setText("Deadzone");
        deadZoneLabel.setVerifyInputWhenFocusTarget(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(deadZoneLabel, gbc);
        dzText = new JFormattedTextField();
        dzText.setHorizontalAlignment(2);
        dzText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(dzText, gbc);
        valueLabel = new JLabel();
        valueLabel.setFocusable(false);
        valueLabel.setHorizontalAlignment(4);
        valueLabel.setPreferredSize(new Dimension(35, 17));
        valueLabel.setText("Value");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(valueLabel, gbc);
        valueText = new JTextField();
        valueText.setEditable(false);
        valueText.setFocusable(false);
        valueText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(valueText, gbc);
        rawLabel = new JLabel();
        rawLabel.setFocusable(false);
        rawLabel.setHorizontalAlignment(4);
        rawLabel.setMinimumSize(new Dimension(32, 17));
        rawLabel.setPreferredSize(new Dimension(32, 17));
        rawLabel.setText("Raw");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(rawLabel, gbc);
        rawText = new JTextField();
        rawText.setEditable(false);
        rawText.setFocusable(false);
        rawText.setPreferredSize(new Dimension(65, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(rawText, gbc);
        progress = new JProgressBar();
        progress.setEnabled(false);
        progress.setFocusable(false);
        progress.setMaximum(1023);
        progress.setMaximumSize(new Dimension(760, 4));
        progress.setMinimum(0);
        progress.setMinimumSize(new Dimension(760, 29));
        progress.setName("Y Axis");
        progress.setPreferredSize(new Dimension(760, 29));
        progress.setValue(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        mainPanel.add(progress, gbc);
        setMinButton = new JButton();
        setMinButton.setHorizontalTextPosition(0);
        setMinButton.setMaximumSize(new Dimension(90, 30));
        setMinButton.setMinimumSize(new Dimension(90, 30));
        setMinButton.setPreferredSize(new Dimension(90, 30));
        setMinButton.setText("Set MIn");
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(setMinButton, gbc);
        setCenterButton = new JButton();
        setCenterButton.setHorizontalAlignment(0);
        setCenterButton.setHorizontalTextPosition(0);
        setCenterButton.setMaximumSize(new Dimension(90, 30));
        setCenterButton.setMinimumSize(new Dimension(90, 30));
        setCenterButton.setPreferredSize(new Dimension(90, 30));
        setCenterButton.setText("Set Center");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(setCenterButton, gbc);
        setMaxButton = new JButton();
        setMaxButton.setHorizontalAlignment(0);
        setMaxButton.setHorizontalTextPosition(0);
        setMaxButton.setMaximumSize(new Dimension(90, 30));
        setMaxButton.setMinimumSize(new Dimension(90, 30));
        setMaxButton.setPreferredSize(new Dimension(90, 30));
        setMaxButton.setText("Set Max");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(setMaxButton, gbc);
        autoLimitCheckBox = new JCheckBox();
        autoLimitCheckBox.setHorizontalAlignment(4);
        autoLimitCheckBox.setMaximumSize(new Dimension(100, 22));
        autoLimitCheckBox.setMinimumSize(new Dimension(100, 22));
        autoLimitCheckBox.setPreferredSize(new Dimension(100, 22));
        autoLimitCheckBox.setText("AutoLimit");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(autoLimitCheckBox, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 180, 0, 0);
        mainPanel.add(spacer1, gbc);
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setHorizontalAlignment(4);
        enabledCheckBox.setMaximumSize(new Dimension(100, 22));
        enabledCheckBox.setMinimumSize(new Dimension(100, 22));
        enabledCheckBox.setPreferredSize(new Dimension(100, 22));
        enabledCheckBox.setText("Enabled");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(enabledCheckBox, gbc);
        hasCenterCheckBox = new JCheckBox();
        hasCenterCheckBox.setHorizontalAlignment(4);
        hasCenterCheckBox.setMaximumSize(new Dimension(100, 22));
        hasCenterCheckBox.setMinimumSize(new Dimension(100, 22));
        hasCenterCheckBox.setPreferredSize(new Dimension(100, 22));
        hasCenterCheckBox.setText("Has Center");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(hasCenterCheckBox, gbc);
        trimComboBox = new JComboBox();
        trimComboBox.setActionCommand("trimChanged");
        trimComboBox.setMinimumSize(new Dimension(38, 30));
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("None");
        defaultComboBoxModel1.addElement("1 bit(/2)");
        defaultComboBoxModel1.addElement("2 bit(/4)");
        defaultComboBoxModel1.addElement("3 bit(/8)");
        defaultComboBoxModel1.addElement("4 bit(/16)");
        defaultComboBoxModel1.addElement("5 bit(/32)");
        defaultComboBoxModel1.addElement("6 bit(/64)");
        defaultComboBoxModel1.addElement("7 bit(/128)");
        trimComboBox.setModel(defaultComboBoxModel1);
        trimComboBox.setPreferredSize(new Dimension(38, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(trimComboBox, gbc);
        trimLabel = new JLabel();
        trimLabel.setText("Trim");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(trimLabel, gbc);
        minLabel.setLabelFor(minText);
        centerLabel.setLabelFor(centerText);
        maxLabel.setLabelFor(maxText);
        deadZoneLabel.setLabelFor(dzText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
