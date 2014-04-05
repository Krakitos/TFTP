package com.funtowiczmo.tftp.ui.logging;

import com.sun.istack.internal.NotNull;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Morgan on 05/04/2014.
 */
public class TextFieldHandler extends Handler {

    private JTextArea output;
    private StringWriter buffer;
    private PrintWriter writer;

    public TextFieldHandler(@NotNull JTextArea output){
        this.output = output;
        this.output.setEditable(false);
    }


    @Override
    public void publish(final LogRecord record) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                buffer = new StringWriter(512);
                writer = new PrintWriter(buffer);

                writer.printf("[%s] [Thread-%d]: %s", record.getLevel(),
                        record.getThreadID(), record.getMessage());
                writer.println();
                output.append(buffer.toString());
            }
        });
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
