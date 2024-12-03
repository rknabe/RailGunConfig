package com.rkade;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.ParseException;

public class IntegerFormatterFactory extends DefaultFormatterFactory {
    private final NumberFormatter formatter;

    public IntegerFormatterFactory(int min, int max) {
        formatter = new NumberFormatter() {
            public Object stringToValue(String text) throws ParseException {
                if (text == null) {
                    return null;
                } else if (text.isEmpty()) {
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(min);
        formatter.setMaximum(max);
        formatter.setAllowsInvalid(false);
        DecimalFormat format = (DecimalFormat) formatter.getFormat();
        format.setGroupingUsed(false); //no commas
    }

    @Override
    public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
        return formatter;
    }
}
