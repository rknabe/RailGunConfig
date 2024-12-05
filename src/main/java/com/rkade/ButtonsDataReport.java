package com.rkade;

import java.nio.ByteBuffer;

public final class ButtonsDataReport extends DataReport {
    private final int buttonsState;
    private final byte shiftButton = 0;
    private final int debounce = 0;
    private final boolean multiplexShifterButtons = false;

    public ButtonsDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);
        buttonsState = buffer.get();
        buffer.rewind();
        System.out.print("buttons:");
        for (byte b : buffer.array()) {
            System.out.print(b);
            System.out.print(':');
        }
        System.out.println();
        /*
        int buttonCount = buffer.limit();
        for (int btnIndex = 0; btnIndex < buttonCount; btnIndex++) {
            byte val = buffer.get();
            buttonStates.add(btnIndex, val > 0);
            System.out.print(val +  " : ") ;
        }
        System.out.println();
        for (int btnIndex = buttonCount; btnIndex < 32; btnIndex++) {
            buttonStates.add(btnIndex, false);
        }*/
    }

    public int getButtonsState() {
        return buttonsState;
    }

    public byte getShiftButton() {
        return shiftButton;
    }

    public int getDebounce() {
        return debounce;
    }

    public boolean isMultiplexShifterButtons() {
        return multiplexShifterButtons;
    }
}
