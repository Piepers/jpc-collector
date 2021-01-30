package me.piepers.jpc.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileImportTest {
  public static void main(String[] args) {
    FileImportTest fit = new FileImportTest();
    fit.readFile();
  }

  private void readFile() {
    // Read the contents from the resources.
    try (InputStream i = FileImportTest.class.getClassLoader().getResourceAsStream("20210118.log");
         InputStreamReader isr = new InputStreamReader(i, StandardCharsets.UTF_8);
         BufferedReader reader = new BufferedReader(isr)) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
        String header = line.substring(0, 8);
        System.out.println("Header is: " + header);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    this.decrypt();
  }

  private String decrypt() {
    char[] mask = {'G', 'r', 'o', 'w', 'a', 't', 't'};
    StringBuilder sb = new StringBuilder();
    for(byte c : "Growatt".getBytes(StandardCharsets.UTF_8)) {
      int i = (int)c;
      sb.append(String.format("%02X", c));
      System.out.println(i);
    }
    System.out.println(sb.toString());
//    String hexMask = String.format("%040x", new BigInteger(1, mask.getBytes(StandardCharsets.UTF_8)));
//    System.out.println("Mask is: " + hexMask);
    return "";
  }
}
