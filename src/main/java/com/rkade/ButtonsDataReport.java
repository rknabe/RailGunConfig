package com.rkade;

import java.nio.ByteBuffer;

public final class ButtonsDataReport extends DataReport {
    private final int buttonsState;
    private final byte shiftButton = 0;
    private final int debounce = 0;
    private final boolean multiplexShifterButtons = false;

    public ButtonsDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);
        //buffer.rewind();
        buttonsState = buffer.get();

        /*System.out.print("buttons:");
        for (byte b : buffer.array()) {
            System.out.print(b);
            System.out.print(':');
        }*/
        //System.out.println(buttonsState);
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
