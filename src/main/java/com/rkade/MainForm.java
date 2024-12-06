package com.rkade;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class MainForm extends BaseForm implements DeviceListener, ActionListener, FocusListener, ChangeListener {
    private final static Logger logger = Logger.getLogger(MainForm.class.getName());
    private final AxisPanel axisPanel = new AxisPanel();
    private JPanel mainPanel;
    private JPanel bottomPanel;
    private JLabel deviceLabel;
    private JLabel statusLabel;
    private JLabel firmwareLabel;
    private JButton defaultsButton;
    private JButton loadButton;
    private JButton saveButton;
    private ButtonsPanel buttonsPanel;
    private JPanel axisPanelParent;
    private JButton btnCalibrate;
    private JButton button2;
    private JPanel northPanel;
    private JPanel buttonPanel;
    private Device device = null;
    private volatile boolean isWaitingOnDevice = false;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public MainForm() {
        try {
            //ImageIcon imageIcon = new ImageIcon(ClassLoader.getSystemResource("wheel55.png"));
            //wheelImage = toBufferedImage(imageIcon.getImage());
            //wheelIconLabel.setIcon(imageIcon);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }

        axisPanelParent.add(axisPanel);

        controls = List.of(btnCalibrate);

        //setupAxisPanels();
        // setupGainPanels();
        setupControlListener();
        setPanelEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean status = handleAction(e);
        if (!status) {
            logger.warning("Action failed for:" + e.getActionCommand());
        }
    }

    private boolean handleAction(ActionEvent e) {
        if (device != null) {
            if (Objects.equals(e.getActionCommand(), btnCalibrate.getActionCommand())) {
                btnCalibrate.setText("Calibrating...");
            }
        }
        return true;
    }

    private void showWaitDialog() {
        JLabel validator = new JLabel("<html><body>Please wait, this may take up to 1 minute.</body></html>");
        JOptionPane pane = new JOptionPane(validator, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, new Object[]{}, null);
        final JDialog dialog = pane.createDialog(mainPanel, "Loading Settings...");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            public Void doInBackground() {
                setPanelEnabled(false);
                int seconds = 0;
                isWaitingOnDevice = true;
                do {
                    try {
                        Thread.sleep(1000);
                        validator.setText(String.format("<html><body>Please wait, this may take up to 1 minute.<br/>Elapsed Seconds: %d</body></html>", ++seconds));
                    } catch (InterruptedException ignored) {
                    }
                } while (isWaitingOnDevice);
                dialog.setVisible(false);
                setPanelEnabled(true);
                return null;
            }
        };
        isWaitingOnDevice = true;
        worker.execute();
        dialog.setVisible(true);
        isWaitingOnDevice = true;
    }

    private boolean handleFocusLost(FocusEvent e) {
        if (device != null) {
        }
        return true;
    }

    @Override
    public void focusLost(FocusEvent e) {
        boolean status = handleFocusLost(e);
        if (!status) {
            logger.warning("Focus lost, failed for:" + e.getSource());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (device != null) {

        }
    }

    @Override
    public void deviceAttached(Device device) {
        this.device = device;
        deviceLabel.setText(device.getName());
        firmwareLabel.setText(device.getFirmwareType() + ":" + device.getFirmwareVersion());
        setPanelEnabled(true);
        buttonsPanel.deviceAttached(device);
    }

    @Override
    public void deviceDetached(Device device) {
        deviceLabel.setText("");
        firmwareLabel.setText("");
        this.device = null;
        setPanelEnabled(false);
        buttonsPanel.deviceDetached(device);
    }

    @Override
    public void deviceUpdated(Device device, String status, DataReport report) {
        if (status != null) {
            statusLabel.setText(status);
        }

        if (report != null) {
            //if (report.getReportType() == Device.DATA_REPORT_ID) {
            switch (report) {
                case ButtonsDataReport buttonsData -> buttonsPanel.deviceUpdated(device, status, buttonsData);
                case SettingsDataReport settings -> {
                    firmwareLabel.setText(settings.getId() + ":" + settings.getVersion());
                    axisPanel.setXAxisMinimum(settings.getXAxisMinimum());
                    axisPanel.setXAxisMaximum(settings.getXAxisMaximum());
                    axisPanel.setYAxisMinimum(settings.getYAxisMinimum());
                    axisPanel.setYAxisMaximum(settings.getYAxisMaximum());
                }
                case AxisDataReport axisData -> axisPanel.deviceUpdated(device, status, axisData);
                default -> {
                }
            }
            //}
            if (isWaitingOnDevice) {
                isWaitingOnDevice = false;
            }
        }
    }

    public JComponent getRootComponent() {
        return mainPanel;
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
        mainPanel.setMinimumSize(new Dimension(1060, 400));
        mainPanel.setPreferredSize(new Dimension(1060, 800));
        buttonsPanel = new ButtonsPanel();
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        bottomPanel.setAutoscrolls(false);
        bottomPanel.setMinimumSize(new Dimension(1060, 75));
        bottomPanel.setPreferredSize(new Dimension(1065, 75));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(bottomPanel, gbc);
        bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panel1.setPreferredSize(new Dimension(445, 32));
        bottomPanel.add(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        deviceLabel = new JLabel();
        deviceLabel.setEnabled(true);
        deviceLabel.setHorizontalAlignment(2);
        deviceLabel.setMinimumSize(new Dimension(145, 20));
        deviceLabel.setPreferredSize(new Dimension(145, 20));
        deviceLabel.setText("Device");
        panel1.add(deviceLabel);
        firmwareLabel = new JLabel();
        firmwareLabel.setEnabled(true);
        firmwareLabel.setFocusable(false);
        firmwareLabel.setHorizontalAlignment(4);
        firmwareLabel.setMaximumSize(new Dimension(70, 17));
        firmwareLabel.setMinimumSize(new Dimension(60, 17));
        firmwareLabel.setPreferredSize(new Dimension(70, 20));
        firmwareLabel.setText("Firmware:");
        panel1.add(firmwareLabel);
        statusLabel = new JLabel();
        statusLabel.setEnabled(true);
        statusLabel.setFocusable(false);
        statusLabel.setHorizontalAlignment(2);
        statusLabel.setMinimumSize(new Dimension(130, 20));
        statusLabel.setPreferredSize(new Dimension(130, 20));
        statusLabel.setRequestFocusEnabled(true);
        statusLabel.setText("Device Not Found...");
        panel1.add(statusLabel);
        defaultsButton = new JButton();
        defaultsButton.setActionCommand("resetDefaults");
        defaultsButton.setMaximumSize(new Dimension(196, 30));
        defaultsButton.setMinimumSize(new Dimension(196, 30));
        defaultsButton.setPreferredSize(new Dimension(196, 30));
        defaultsButton.setText("Reset Settings to Defaults");
        bottomPanel.add(defaultsButton);
        loadButton = new JButton();
        loadButton.setActionCommand("loadEEPROM");
        loadButton.setMaximumSize(new Dimension(196, 30));
        loadButton.setMinimumSize(new Dimension(196, 30));
        loadButton.setPreferredSize(new Dimension(196, 30));
        loadButton.setText("Load Settings From EEPROM");
        bottomPanel.add(loadButton);
        saveButton = new JButton();
        saveButton.setActionCommand("saveSettings");
        saveButton.setHorizontalAlignment(0);
        saveButton.setMaximumSize(new Dimension(196, 30));
        saveButton.setMinimumSize(new Dimension(196, 30));
        saveButton.setPreferredSize(new Dimension(196, 30));
        saveButton.setText("Save Settings to EEPROM");
        bottomPanel.add(saveButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}