package com.rkade;

import java.nio.ByteBuffer;

public final class ButtonsDataReport extends DataReport {
    private final int buttonsState;
    private final byte shiftButton = 0;
    private final int debounce = 0;
    private final boolean multiplexShifterButtons = false;

    public ButtonsDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        buttonsState = buffer.get();
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
