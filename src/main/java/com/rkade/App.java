package com.rkade;

import com.formdev.flatlaf.intellijthemes.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    public static final String CL_PARAM_SPRING_ON = "springon";
    public static final String CL_PARAM_SPRING_OFF = "springoff";
    public static final String CL_PARAM_AUTO_CENTER = "autocenter";
    public static final String CL_PARAM_CENTER = "center";
    public static final String CL_PARAM_HELP = "help";

    public static void main(String[] args) {
        boolean showGui = true;
        System.setProperty("fazecast.jSerialComm.appid", "com.rkade.RKadeGunConfig");

        /*
        Options options = setupCommandLineOptions();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cl = parser.parse(options, args);

            if (cl.hasOption(CL_PARAM_HELP)) {
                HelpFormatter fmt = new HelpFormatter();
                fmt.printHelp("Help", options);
                System.exit(1);
            }

            if (cl.hasOption(CL_PARAM_SPRING_ON) || cl.hasOption(CL_PARAM_SPRING_OFF)
                    || cl.hasOption(CL_PARAM_AUTO_CENTER) || cl.hasOption(CL_PARAM_CENTER)) {
                showGui = false;
                int failed = 0;
                Device device = DeviceManager.openDevice();
                if (device != null) {
                    if (cl.hasOption(CL_PARAM_SPRING_ON)) {
                        if (device.setConstantSpring(true)) {
                            System.out.println("Constant Spring enabled");
                        } else {
                            failed = 1;
                            System.out.println("Error enabling Constant Spring");
                        }
                    }
                    if (cl.hasOption(CL_PARAM_SPRING_OFF)) {
                        if (device.setConstantSpring(false)) {
                            System.out.println("Constant Spring disabled");
                        } else {
                            failed = 1;
                            System.out.println("Error disabling Constant Spring");
                        }
                    }
                    if (cl.hasOption(CL_PARAM_AUTO_CENTER)) {
                        if (device.runAutoCenter()) {
                            System.out.println("AutoCenter complete");
                        } else {
                            failed = 1;
                            System.out.println("Error running AutoCenter");
                        }
                    }
                    if (cl.hasOption(CL_PARAM_CENTER)) {
                        if (device.setWheelCenterCli()) {
                            System.out.println("Wheel center set to current position");
                        } else {
                            failed = 1;
                            System.out.println("Error setting wheel center");
                        }
                    }
                } else {
                    failed = 1;
                    System.out.println("Could not open device for cli");
                }
                System.exit(failed);
            }
        } catch (UnrecognizedOptionException e) {
            HelpFormatter fmt = new HelpFormatter();
            fmt.printHelp("Help", options);
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        */

        if (showGui) {
            try {
                InputStream is = App.class.getResourceAsStream("/logging.properties");
                LogManager.getLogManager().readConfiguration(is);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Logger logger = Logger.getLogger(App.class.getName());

            SwingUtilities.invokeLater(() -> {
                try {
                    //com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme.setup();
                    //com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme.setup();
                    FlatMaterialDesignDarkIJTheme.setup();
                    JFrame frame = new JFrame("RKADE Gun Config");
                    MainForm mainForm = new MainForm();
                    frame.setContentPane(mainForm.getRootComponent());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    Image icon = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("crosshair.png"));
                    frame.setIconImage(icon);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);

                    mainForm.setDeviceManager(new DeviceManager(mainForm));
                } catch (Exception ex) {
                    logger.warning(ex.getMessage());
                }
            });
        }
    }

    private static Options setupCommandLineOptions() {
        Options options = new Options();

        Option springOnOption = new Option(CL_PARAM_SPRING_ON, "Turn on constant spring effect");
        springOnOption.setRequired(false);
        springOnOption.setOptionalArg(false);
        options.addOption(springOnOption);

        Option springOffOption = new Option(CL_PARAM_SPRING_OFF, "Turn off constant spring effect");
        springOffOption.setRequired(false);
        springOffOption.setOptionalArg(false);
        options.addOption(springOffOption);

        Option autocenterOption = new Option(CL_PARAM_AUTO_CENTER, "Perform automatic range and center calibration");
        autocenterOption.setRequired(false);
        autocenterOption.setOptionalArg(false);
        options.addOption(autocenterOption);

        Option centerOption = new Option(CL_PARAM_CENTER, "Set wheel center to current position");
        centerOption.setRequired(false);
        centerOption.setOptionalArg(false);
        options.addOption(centerOption);

        Option helpOption = Option.builder("h")
                .longOpt(CL_PARAM_HELP)
                .required(false)
                .hasArg(false)
                .build();
        options.addOption(helpOption);

        return options;
    }
}
