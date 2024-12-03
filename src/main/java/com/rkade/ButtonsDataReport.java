package com.rkade;

import java.nio.ByteBuffer;

public final class ButtonsDataReport extends DataReport {
    private final int buttonsState;
    private final byte shiftButton;
    private final int debounce;
    private final boolean multiplexShifterButtons;

    public ButtonsDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);
        buttonsState = buffer.getInt();
        shiftButton = buffer.get();
        debounce = Byte.toUnsignedInt(buffer.get());
        multiplexShifterButtons = buffer.get() > 0;
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
