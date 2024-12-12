package com.rkade;

import com.fazecast.jSerialComm.SerialPort;
import io.github.libsdl4j.api.haptic.SDL_Haptic;
import io.github.libsdl4j.api.haptic.SDL_HapticEffect;
import io.github.libsdl4j.api.joystick.SDL_Joystick;
import purejavahidapi.HidDevice;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.SdlSubSystemConst.*;
import static io.github.libsdl4j.api.haptic.SDL_HapticDirectionEncoding.SDL_HAPTIC_CARTESIAN;
import static io.github.libsdl4j.api.haptic.SDL_HapticEffectType.*;
import static io.github.libsdl4j.api.haptic.SdlHaptic.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;

public class Device {
    public static final byte CMD_REPORT_ID = 15;
    public static final byte DATA_REPORT_ID = 1;
    public static final byte DATA_REPORT_VALUE_COUNT = 31;
    public static final byte CMD_GET_VER = 16;
    public static final byte CMD_GET_SETTINGS = 1;
    public static final byte CMD_HEARTBEAT = 0;
    public static final byte CMD_SET_AXIS_LIMITS = 2;
    public static final byte CMD_SET_AUTO_RECOIL = 3;
    public static final byte CMD_SET_TRIGGER_RATE = 4;
    public static final byte CMD_SET_TRIGGER_HOLD = 5;
    public static final byte CMD_EESAVE = 16;
    public static final byte CMD_EELOAD = 17;
    public static final byte CMD_DEFAULT = 18;
    public static final byte CMD_RECOIL = 19;
    public static final byte CMD_GET_MISC = 6;
    public static final byte CMD_SET_RANGE = 10;
    public static final byte CMD_SET_AALIMITS = 11;
    public static final byte CMD_SET_AACENTER = 12;
    public static final byte CMD_SET_AADZ = 13;
    public static final byte CMD_SET_AAAUTOLIM = 14;
    public static final byte CMD_CENTER = 23;
    public static final byte CMD_WHEEL_LIMITS = 24;
    public static final byte CMD_WHEEL_CENTER = 25;
    public static final byte CMD_WHEEL_DZ = 26;
    public static final byte CMD_WHEEL_AUTO_LIMIT = 27;
    public static final byte CMD_WHEEL_TRIM = 28;
    public static final byte CMD_WHEEL_INVERT = 29;
    public static final String CMD_AUTOCENTER_TEXT = "autocenter ";
    public static final String CMD_CENTER_TEXT = "center ";
    public static final String CMD_SPRING_ON_TEXT = "spring 1 ";
    public static final String CMD_SPRING_OFF_TEXT = "spring 0 ";
    public static final String CMD_VERSION = "version ";
    public static final String SUPPORTED_FIRMWARE_TYPE = "RKADE";
    private static final Logger logger = Logger.getLogger(Device.class.getName());
    private static final int WAIT_AFTER_EFFECT_UPDATE = 5;
    private final String hidPath;
    private final HidDevice hidDevice;
    private String name;
    private SDL_Haptic hapticJoystick;
    private SerialPort port;
    private String firmwareType;
    private String firmwareVersion;
    private int sineEffectId = -1;
    private int springEffectId = -1;
    private int rampEffectId = -1;
    private int frictionEffectId = -1;
    private int constantEffectLeftId = -1;
    private int constantEffectRightId = -1;
    private int sawtoothUpEffectId = -1;
    private int sawtoothDownEffectId = -1;
    private int inertiaEffectId = -1;
    private int damperEffectId = -1;
    private int triangleEffectId = -1;

    public Device(HidDevice hidDevice, String path) {
        this.hidDevice = hidDevice;
        this.name = hidDevice.getHidDeviceInfo().getProductString();
        this.hidPath = path;
    }

    @Override
    public String toString() {
        return name;
    }

    public synchronized boolean saveSettings() {
        return sendCommand(CMD_EESAVE);
    }

    public synchronized boolean loadDefaults() {
        return sendCommand(CMD_DEFAULT);
    }

    public synchronized boolean loadFromEeprom() {
        return sendCommand(CMD_EELOAD);
    }

    public synchronized boolean setAutoRecoil(boolean state) {
        return sendCommand(CMD_SET_AUTO_RECOIL, state);
    }

    public synchronized boolean doRecoil(boolean state) {
        return sendCommand(CMD_RECOIL, state);
    }

    public synchronized boolean setWheelCenter() {
        return sendCommand(CMD_CENTER);
    }

    public synchronized boolean setWheelCenterCli() {
        return writeTextToPort(CMD_CENTER_TEXT);
    }

    public synchronized boolean setWheelRange(Short range) {
        return sendCommand(CMD_SET_RANGE, range);
    }

    public synchronized boolean setWheelLimits(Short minValue, short maxValue) {
        return sendCommand(CMD_WHEEL_LIMITS, minValue, maxValue);
    }

    public synchronized boolean setWheelCenter(short center) {
        return sendCommand(CMD_WHEEL_CENTER, center);
    }

    public synchronized boolean setWheelDeadZone(short deadZone) {
        return sendCommand(CMD_WHEEL_DZ, deadZone);
    }

    public synchronized boolean setWheelAutoLimit(short flag) {
        return sendCommand(CMD_WHEEL_AUTO_LIMIT, flag);
    }

    public synchronized boolean setWheelTrim(short trimIndex) {
        return sendCommand(CMD_WHEEL_TRIM, trimIndex);
    }

    public synchronized boolean setWheelInvert(short state) {
        return sendCommand(CMD_WHEEL_INVERT, state);
    }

    public synchronized boolean setTriggerRepeatRate(short rate) {
        return sendCommand(CMD_SET_TRIGGER_RATE, rate);
    }

    public synchronized boolean setTriggerHoldTime(short time) {
        System.out.println("sending trigger hold:" + time);
        return sendCommand(CMD_SET_TRIGGER_HOLD, time);
    }

    public synchronized boolean setAxisLimits(short xMinValue, short xMaxValue, short yMinValue, short yMaxValue) {
        return sendCommand(CMD_SET_AXIS_LIMITS, xMinValue, xMaxValue, yMinValue, yMaxValue);
    }

    public synchronized boolean setConstantSpring(boolean state) {
        if (state) {
            return writeTextToPort(CMD_SPRING_ON_TEXT);
        }
        return writeTextToPort(CMD_SPRING_OFF_TEXT);
    }

    public synchronized boolean writeTextToPort(String text) {
        boolean isOpen = port.isOpen();
        if (!isOpen) {
            port.setBaudRate(9600);
            port.setParity(0);
            port.setNumStopBits(1);
            port.setNumDataBits(8);
            isOpen = port.openPort(500);
        }
        if (isOpen) {
            byte[] value = text.getBytes(StandardCharsets.US_ASCII);
            int ret = port.writeBytes(value, value.length);
            port.closePort();
            return (ret > 0);
        }
        return false;
    }

    public synchronized String readVersion() {
        boolean isOpen = port.isOpen();
        if (!isOpen) {
            port.setBaudRate(9600);
            port.setParity(0);
            port.setNumStopBits(1);
            port.setNumDataBits(8);
            isOpen = port.openPort(500);
        }
        if (isOpen) {
            byte[] value = CMD_VERSION.getBytes(StandardCharsets.US_ASCII);
            int ret = port.writeBytes(value, value.length);
            if (ret > 0) {
                try {
                    InputStream is = port.getInputStream();
                    port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                    InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    return bufferedReader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    port.closePort();
                }
            }
            return null;
        }
        return null;
    }

    public synchronized boolean runAutoCenter() {
        return writeTextToPort(CMD_AUTOCENTER_TEXT);
    }

    public void setPort(SerialPort port) {
        this.port = port;
    }

    private boolean sendCommand(byte command) {
        return sendCommand(command, (short) 0, (short) 0, (short) 0);
    }

    private boolean sendCommand(byte command, boolean state) {
        return sendCommand(command, (short) (state ? 1 : 0), (short) 0);
    }

    private boolean sendCommand(byte command, short arg1) {
        return sendCommand(command, arg1, (short) 0, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2) {
        return sendCommand(command, arg1, arg2, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2, short arg3) {
        return sendCommand(command, arg1, arg2, arg3, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2, short arg3, short arg4) {
        final boolean[] status = {true};
        //TODO: this will not work with CLI only invocation
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            public Void doInBackground() {
                byte[] data = new byte[9];
                data[0] = command;
                data[1] = getFirstByte(arg1);
                data[2] = getSecondByte(arg1);

                data[3] = getFirstByte(arg2);
                data[4] = getSecondByte(arg2);

                data[5] = getFirstByte(arg3);
                data[6] = getSecondByte(arg3);

                data[7] = getFirstByte(arg4);
                data[8] = getSecondByte(arg4);

                int ret = hidDevice.setOutputReport(CMD_REPORT_ID, data, 9);
                if (ret <= 0) {
                    logger.severe("Device returned error on Save:" + ret);
                    status[0] = false;
                }
                return null;
            }
        };
        worker.execute();
        return status[0];
    }

    private byte getFirstByte(short value) {
        return (byte) (value & 0xff);
    }

    private byte getSecondByte(short value) {
        return (byte) ((value >> 8) & 0xff);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    private synchronized SDL_Haptic getHapticJoystick() {
        if (hapticJoystick == null) {
            int ret = SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_HAPTIC | SDL_INIT_GAMECONTROLLER);
            if (ret != 0) {
                logger.severe("Could not initialize SDL");
                return null;
            }
            int numJoysticks = SDL_NumJoysticks();
            SDL_Joystick arduinoFfb = null;
            for (int i = 0; i < numJoysticks; i++) {
                SDL_Joystick gameController = SDL_JoystickOpen(i);
                String sdlDevicePath = SDL_JoystickPath(gameController);
                if (hidPath.equals(sdlDevicePath)) {
                    arduinoFfb = gameController;
                    break;
                }
                SDL_JoystickClose(gameController);
            }
            hapticJoystick = SDL_HapticOpenFromJoystick(arduinoFfb);
        }
        return hapticJoystick;
    }

    public synchronized boolean doFfbSine() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (sineEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_SINE);
            sineEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.periodic.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.periodic.direction.dir[0] = 1;
            effect.constant.direction.dir[1] = 0; //Y Position
            effect.periodic.period = 100;
            effect.periodic.magnitude = 9000;
            effect.periodic.length = 2000;
            effect.periodic.attackLength = 120;
            effect.periodic.fadeLength = 120;
            SDL_HapticUpdateEffect(ffbDevice, sineEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, sineEffectId, 1) == 0;
    }

    public synchronized boolean doFfbSpring() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (springEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_SPRING);
            springEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.condition.delay = 0;
            effect.condition.length = 5000;
            effect.condition.direction.dir[0] = 1;
            effect.constant.direction.dir[1] = 1; //Y Position
            effect.condition.leftCoeff[0] = (short) (30000);
            effect.condition.rightCoeff[0] = (short) (30000);
            effect.condition.leftSat[0] = (short) ((30000) * 10);
            effect.condition.rightSat[0] = (short) ((30000) * 10);
            effect.condition.center[0] = 0;
            SDL_HapticUpdateEffect(ffbDevice, springEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, springEffectId, 1) == 0;
    }

    public synchronized boolean doFfbRamp() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (rampEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_RAMP);
            rampEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.ramp.type = SDL_HAPTIC_RAMP;
            effect.ramp.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.ramp.delay = 0;
            effect.ramp.length = 8000;
            effect.ramp.start = 0x4000;
            effect.ramp.end = -0x4000;
            SDL_HapticUpdateEffect(ffbDevice, rampEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, rampEffectId, 1) == 0;
    }

    public synchronized boolean doFfbConstantLeft() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (constantEffectLeftId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_CONSTANT);
            constantEffectLeftId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.constant.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.constant.direction.dir[0] = 1;
            effect.constant.length = 500;
            effect.constant.delay = 0;
            effect.constant.level = 8000;
            SDL_HapticUpdateEffect(ffbDevice, constantEffectLeftId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, constantEffectLeftId, 1) == 0;
    }

    public synchronized boolean doFfbConstantRight() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (constantEffectRightId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_CONSTANT);
            constantEffectRightId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.constant.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.constant.direction.dir[0] = -1;
            effect.constant.length = 500;
            effect.constant.delay = 0;
            effect.constant.level = -8000;
            SDL_HapticUpdateEffect(ffbDevice, constantEffectRightId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, constantEffectRightId, 1) == 0;
    }


    public synchronized boolean doFfbFriction() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (frictionEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_FRICTION);
            frictionEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.condition.delay = 0;
            effect.condition.length = 5000;
            effect.condition.direction.dir[0] = 1; // not used
            effect.constant.direction.dir[1] = 0; //Y Position
            effect.condition.leftSat[0] = (short) 0xFFFF;
            effect.condition.rightSat[0] = (short) 0xFFFF;
            effect.condition.leftCoeff[0] = (short) 32767.0;
            effect.condition.rightCoeff[0] = (short) 32767.0;
            SDL_HapticUpdateEffect(ffbDevice, frictionEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, frictionEffectId, 1) == 0;
    }

    public synchronized boolean doFfbSawtoothUp() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (sawtoothUpEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_SAWTOOTHUP);
            sawtoothUpEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.type = SDL_HAPTIC_SAWTOOTHUP;
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.periodic.period = 500;
            effect.periodic.magnitude = 0x5000;
            effect.periodic.length = 5000;
            effect.periodic.attackLength = 1000;
            effect.periodic.fadeLength = 1000;
            SDL_HapticUpdateEffect(ffbDevice, sawtoothUpEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, sawtoothUpEffectId, 1) == 0;
    }

    public synchronized boolean doFfbSawtoothDown() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (sawtoothDownEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_SAWTOOTHDOWN);
            sawtoothDownEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.type = SDL_HAPTIC_SAWTOOTHDOWN;
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.periodic.period = 500;
            effect.periodic.magnitude = 0x5000;
            effect.periodic.length = 5000;
            effect.periodic.attackLength = 1000;
            effect.periodic.fadeLength = 1000;
            SDL_HapticUpdateEffect(ffbDevice, sawtoothDownEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, sawtoothDownEffectId, 1) == 0;
    }

    public synchronized boolean doFfbInertia() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (inertiaEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_INERTIA);
            inertiaEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.type = SDL_HAPTIC_INERTIA;
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.condition.delay = 0;
            effect.condition.length = 5000;
            effect.condition.direction.dir[0] = 1; // not used
            effect.constant.direction.dir[1] = 0; //Y Position
            effect.condition.leftSat[0] = (short) 0xFFFF;
            effect.condition.rightSat[0] = (short) 0xFFFF;
            effect.condition.leftCoeff[0] = (short) 32767.0;
            effect.condition.rightCoeff[0] = (short) 32767.0;
            SDL_HapticUpdateEffect(ffbDevice, inertiaEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, inertiaEffectId, 1) == 0;
    }

    public synchronized boolean doFfbDamper() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (damperEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_DAMPER);
            damperEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.type = SDL_HAPTIC_DAMPER;
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.condition.delay = 0;
            effect.condition.length = 5000;
            effect.condition.direction.dir[0] = 1; // not used
            effect.constant.direction.dir[1] = 0; //Y Position
            effect.condition.leftSat[0] = (short) 0xFFFF;
            effect.condition.rightSat[0] = (short) 0xFFFF;
            effect.condition.leftCoeff[0] = (short) 32767.0;
            effect.condition.rightCoeff[0] = (short) 32767.0;
            SDL_HapticUpdateEffect(ffbDevice, damperEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, damperEffectId, 1) == 0;
    }

    public synchronized boolean doFfbTriangle() {
        SDL_Haptic ffbDevice = getHapticJoystick();
        if (triangleEffectId < 0) {
            SDL_HapticEffect effect = createEffect(SDL_HAPTIC_TRIANGLE);
            triangleEffectId = SDL_HapticNewEffect(ffbDevice, effect);
            effect.condition.type = SDL_HAPTIC_TRIANGLE;
            effect.condition.direction.type = SDL_HAPTIC_CARTESIAN;
            effect.periodic.period = 500;
            effect.periodic.magnitude = 0x5000;
            effect.periodic.length = 5000;
            effect.periodic.attackLength = 1000;
            effect.periodic.fadeLength = 1000;
            SDL_HapticUpdateEffect(ffbDevice, triangleEffectId, effect);
            //seems at least 2 milliseconds sleep needed after update
            sleep(WAIT_AFTER_EFFECT_UPDATE);
        }
        return SDL_HapticRunEffect(ffbDevice, triangleEffectId, 1) == 0;
    }

    private SDL_HapticEffect createEffect(int type) {
        SDL_HapticEffect effect = new SDL_HapticEffect();
        //cannot set this directly, or it is zeroed out by HapticNewEffect call
        effect.writeField("type", (short) type);
        return effect;
    }
}
