/**
 * Quadrofly Software (http://quadrofly.ni-c.de)
 *
 * @file     Quadrometrics.java
 */
package com.quadrometrics;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JTextField;

import org.swixml.SwingEngine;

/**
 * 
 * 
 * @author Willi Thiel (wthiel@quadrofly.ni-c.de)
 * @date Apr 7, 2012
 * 
 */
public class Quadrometrics {

    private SwingEngine swix;

    private static StringBuilder s = new StringBuilder();

    public static JTextField rc1;
    public static JTextField rc2;
    public static JTextField rc3;
    public static JTextField rc4;

    public static JTextField accx;
    public static JTextField accy;
    public static JTextField accz;

    public static JTextField gyrox;
    public static JTextField gyroy;
    public static JTextField gyroz;

    public static JTextField pid0;
    public static JTextField pid1;
    public static JTextField pid2;
    public static JTextField pid3;

    public static JTextField speed0;
    public static JTextField speed1;
    public static JTextField speed2;
    public static JTextField speed3;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            (new Quadrometrics()).connect("/dev/ttyUSB1");
        } catch (Exception e1) {
            try {
                (new Quadrometrics()).connect("/dev/ttyUSB0");
            } catch (Exception e2) {
                System.err.println("No port found");
            }
        }
    }

    public Quadrometrics() {
        try {
            swix = new SwingEngine(this);
            swix.render("com/quadrometrics/view/Quadrometrics.xml");
            swix.getRootComponent().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();

                (new Thread(new SerialReader(in))).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialReader implements Runnable {
        InputStream in;

        byte[] serialbuffer = new byte[16];
        int bufferPos = 0;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    System.out.print(new String(buffer, 0, len));
                    for (int i = 0; i < len; i++) {
                        s.append(new String(buffer, i, 1));
                        if (new String(buffer, i, 1).equals("\n")) {
                            String[] tmp = s.toString().split(";");
                            try {
                                if (tmp[0].equals("RC")) {
                                    rc1.setText(tmp[1]);
                                    rc2.setText(tmp[2]);
                                    rc3.setText(tmp[3]);
                                    rc4.setText(tmp[4]);
                                }
                                if (tmp[0].equals("MPU")) {
                                    accx.setText(tmp[1]);
                                    accy.setText(tmp[2]);
                                    accz.setText(tmp[3]);
                                    gyrox.setText(tmp[5]);
                                    gyroy.setText(tmp[6]);
                                    gyroz.setText(tmp[7]);
                                }
                                if (tmp[0].equals("PID")) {
                                    if (!tmp[1].equals("0")) {
                                        pid0.setText(String.valueOf(Double.valueOf(tmp[1]) / 100));
                                    }
                                    if (!tmp[2].equals("0")) {
                                        pid1.setText(String.valueOf(Double.valueOf(tmp[2]) / 100));
                                    }
                                    if (!tmp[3].equals("0")) {
                                        pid2.setText(String.valueOf(Double.valueOf(tmp[3]) / 100));
                                    }
                                }
                                if (tmp[0].equals("SPEED")) {
                                    speed0.setText(tmp[1]);
                                    speed1.setText(tmp[2]);
                                    speed2.setText(tmp[3]);
                                    speed3.setText(tmp[4]);
                                }
                            } catch (Exception e) {
                            }
                            s = new StringBuilder();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
