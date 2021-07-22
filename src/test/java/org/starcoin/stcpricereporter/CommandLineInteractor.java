package org.starcoin.stcpricereporter;

import java.io.*;

public class CommandLineInteractor {

    private Process process;

    private BufferedReader reader;
    private BufferedWriter writer;


    public CommandLineInteractor(Process process) {
        this.process = process;
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    public CommandLineInteractor expect(String expectFor, int timeOutSeconds) {
        boolean ok = false;
        try {
            ok = expect(this.reader, expectFor, timeOutSeconds);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (!ok) {
            throw new RuntimeException("Expect for: " + expectFor + ", but failed.");
        }
        return this;
    }

    public CommandLineInteractor sendLine(String line) {
        try {
            sendLine(this.writer, line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return this;
    }

    private static void sendLine(BufferedWriter writer, String outputLine) throws IOException {
        System.out.println("Send: " + outputLine);
        writer.write(outputLine);
        writer.newLine();
        writer.flush();
    }

    private static boolean expect(BufferedReader reader, String expectFor, int timeOutSeconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        boolean expectOk = false;
        StringBuilder inputSB = new StringBuilder();
        while (true) {
            String bufferStr = null;
            while (reader.ready()) {
                char[] charBuffer = new char[200];
                int len = reader.read(charBuffer, 0, charBuffer.length);
                bufferStr = new String(charBuffer, 0, len);
                //System.out.println("----" + bufferStr);
                System.out.print(bufferStr);
                inputSB.append(bufferStr);
            }
            if (bufferStr == null) {
                //System.out.println("=========== buffer str is null");
                Thread.sleep(100);
            }
            String inputStr = inputSB.toString();
            //System.out.println(inputStr);
            if (inputStr.contains(expectFor)) {
                expectOk = true;
                break;
            }
            if (startTime + timeOutSeconds * 1000 < System.currentTimeMillis()) {
                break;
            }
        }
        return expectOk;
    }

    public CommandLineInteractor waitSeconds(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return this;
    }
}
