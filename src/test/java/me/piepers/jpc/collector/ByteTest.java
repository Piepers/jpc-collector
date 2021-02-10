package me.piepers.jpc.collector;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ByteTest {
  @Test
  public void test_that_bytes_are_converted_as_expected() {
    byte value = (byte) 170;
//    int b = 0x83;
//    System.out.println(a);
//    System.out.println(b);

    System.out.println("Byte Unsigned: " + Byte.toUnsignedInt(value) + "\tSigned: " + value);
    System.out.println("    Hex value:  0x" + Integer.toHexString(value & 0xFF));
    System.out.println("    Hex value:  0x" + Integer.toHexString(value));
    System.out.println("    Binair:     " + Integer.toBinaryString(value & 0xFF));
  }

  @Test
  public void test_convert_to_unsigned_int() {
//    int[] numbers = {1, 109, 0, 6, 1, 95, 1, 4, 13, 34, 44, 66, 88, 65, 70, 117, 69, 91, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 43, 61, 36, 71, 53, 118, 64, 95, 52, 35, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 116, 117, 104, 87, 86, 102, 116, 97, 116, 116, 107, 114, 110, 119, 97, 116, 116, 68, -18, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 103, -63, 123, 117, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 69, 114, 111, 110, 112, 117, 91, -41, 98, 111, -113, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 117, 20, 124, 109, 119, 97, 116, 116, 71, 95, 111, 46, 47, 84, 116, 71, 114, 111, 119, 97, 116, 118, 71, 114, 118, 47, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 94, 42, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 59, 116, -14, 71, 107, 125, 113, 113, 99, 100, 112, 114, 111, 119, 120, 102, 114, 87, 101, 97, 104, 97, 116, 116, 94, 96, 107, 126, 103, 89, 96, 71, 114, 111, 110, 115, 112, 114, 70, 84, 70, 119, 97, 116, 109, 85, 113, 121, 117, 69, 105, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 58, -8};
    int[] numbers = {0, -9, 0, 6, 1, 95, 1, 4, 13, 34, 44, 66, 88, 65, 70, 117, 69, 91, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 43, 61, 36, 71, 53, 118, 64, 95, 52, 35, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 116, 117, 104, 73, 109, 123, 116, 97, 116, 116, 107, 114, 110, 119, 97, 118, 115, 65, 72, 111, 116, 97, 116, 118, 64, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 96, -119, 103, -50, 123, 89, 119, 99, 116, 116, 69, 126, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 69, 114, 111, 110, 112, 117, 91, 18, -1, 110, 117, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 117, 31, 124, 114, 119, 97, 116, 116, 71, 95, 111, 46, 47, 84, 116, 71, 114, 111, 119, 97, 116, 118, 71, 114, 118, 47, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 94, 42, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 119, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 59, 116, -14, 71, 107, 125, 113, 113, 99, 100, 112, 114, 111, 119, 120, 102, 114, 87, 101, 97, 104, 97, 116, 116, 94, 96, 107, 126, 103, 89, 96, 71, 114, 111, 110, 115, 112, 114, 70, 84, 70, 119, 97, 116, 109, 85, 113, 121, 117, 69, 105, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 105, 80};
    int[] unumbers = new int[numbers.length];

    for (int i = 0; i < numbers.length; i++) {
      byte b = (byte) numbers[i];
      unumbers[i] = Byte.toUnsignedInt(b);
    }

    System.out.print(Arrays.toString(unumbers));
  }

  @Test
  public void decrypt() {
    byte[] expectedDataRecord40 = {0x01, 0x40};
    byte[] expectedDataRecord50 = {0x01, 0x40};

    int[] numbers = {1, 146, 0, 6, 1, 95, 1, 80, 13, 34, 44, 66, 88, 65, 70, 117, 69, 91, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 43, 61, 36, 71, 53, 118, 64, 95, 52, 35, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 117, 114, 102, 75, 96, 66, 116, 97, 116, 116, 107, 114, 110, 119, 97, 116, 58, 65, 112, 111, 119, 97, 116, 116, 9, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 57, 103, 206, 123, 67, 119, 97, 116, 116, 71, 46, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 108, 114, 111, 115, 171, 116, 81, 65, 14, 111, 152, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 117, 1, 124, 121, 119, 97, 116, 116, 71, 95, 111, 46, 47, 84, 116, 71, 114, 111, 119, 97, 116, 88, 71, 114, 107, 160, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 67, 165, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 59, 116, 242, 71, 107, 125, 118, 116, 117, 84, 93, 114, 111, 119, 120, 102, 117, 82, 115, 117, 77, 97, 116, 116, 94, 96, 110, 98, 96, 109, 107, 71, 114, 111, 110, 115, 117, 96, 82, 73, 118, 119, 97, 116, 109, 85, 115, 96, 100, 124, 122, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 116, 71, 114, 111, 119, 97, 116, 63, 149};
    char[] mask = {'G', 'r', 'o', 'w', 'a', 't', 't'};
    // Separate unscrambled int array probably not necessary as the number will be converted to hex later on so that they can be inspected at certain positions. Thereafter most of them will also be converted to decimals anyway.
    int[] unscrambled = new int[numbers.length];
    StringBuilder uss = new StringBuilder();
    // Do not unscramble the first 8 positions.
    for (int i = 0; i < 8; i++) {
      unscrambled[i] = numbers[i];
      uss.append(String.format("%02X", numbers[i]));
    }

    // Eerst omzetten naar hexmask is niet nodig voor ontcijferen.
    StringBuffer hexMask = new StringBuffer();

    for (char c : mask) {
      int i = (int) c;
      hexMask.append(Integer.toHexString(i));
    }
    System.out.println(hexMask.toString());

    for (int i = 8, j = 0; i < numbers.length; i++, j++) {
      if (j == mask.length) {
        j = 0;
      }
      unscrambled[i] = numbers[i] ^ (byte) mask[j];
      uss.append(String.format("%02X", unscrambled[i]));
    }

    System.out.println("Unscrambled: " + Arrays.toString(unscrambled));
    System.out.println("As hex string: " + uss.toString());

    String result = uss.toString();
    System.out.println("Record type is: " + uss.substring(12, 16));
    Map<String, Integer> struct0104 = new HashMap<>() {{
      put("pvserial", 76);
      put("date", 0);
      put("pvstatus", 158);
      put("pvpowerin", 162);
      put("pv1voltage", 170);
      put("pv1current", 174);
      put("pv1watt", 178);
      put("pv2voltage", 186);
      put("pv2current", 190);
      put("pv2watt", 194);
      put("pvpowerout", 202);
      put("pvfrequency", 210);
      put("pvgridvoltage", 214);
      put("pvenergytoday", 262);
      put("pvenergytotal", 270);
      put("pvtemperature", 286);
      put("pvipmtemperature", 322);
    }};

    // Note: the scructure below is identical to the 0104 but the date seems to be present here.
    Map<String, Integer> struct0150 = new HashMap<>() {{
      put("pvserial", 76);
      put("date", 136);
      put("pvstatus", 158);
      put("pvpowerin", 162);
      put("pv1voltage", 170);
      put("pv1current", 174);
      put("pv1watt", 178);
      put("pv2voltage", 186);
      put("pv2current", 190);
      put("pv2watt", 194);
      put("pvpowerout", 202);
      put("pvfrequency", 210);
      put("pvgridvoltage", 214);
      put("pvenergytoday", 262);
      put("pvenergytotal", 270);
      put("pvtemperature", 286);
      put("pvipmtemperature", 322);
    }};

    // Get the contents from the arrays on certain positions.
    String pvserial = result.substring(struct0150.get("pvserial"), struct0150.get("pvserial") + 20);
    int pvstatus = Integer.valueOf(result.substring(struct0150.get("pvstatus"), struct0150.get("pvstatus") + 4), 16);
    Long pvpowerin = Long.parseLong(result.substring(struct0150.get("pvpowerin"), struct0150.get("pvpowerin") + 8), 16);
    int pv1voltage = Integer.valueOf(result.substring(struct0150.get("pv1voltage"), struct0150.get("pv1voltage") + 4), 16);
    int pv1current = Integer.valueOf(result.substring(struct0150.get("pv1current"), struct0150.get("pv1current") + 4), 16);
    Long pv1watt = Long.parseLong(result.substring(struct0150.get("pv1watt"), struct0150.get("pv1watt") + 8), 16);
    int pv2voltage = Integer.valueOf(result.substring(struct0150.get("pv2voltage"), struct0150.get("pv2voltage") + 4), 16);
    int pv2current = Integer.valueOf(result.substring(struct0150.get("pv2current"), struct0150.get("pv2current") + 4), 16);
    Long pv2watt = Long.parseLong(result.substring(struct0150.get("pv2watt"), struct0150.get("pv2watt") + 8), 16);
    Long pvpowerout = Long.parseLong(result.substring(struct0150.get("pvpowerout"), struct0150.get("pvpowerout") + 8), 16);
    int pvfrequentie = Integer.valueOf(result.substring(struct0150.get("pvfrequency"), struct0150.get("pvfrequency") + 4), 16);
    int pvgridvoltage = Integer.valueOf(result.substring(struct0150.get("pvgridvoltage"), struct0150.get("pvgridvoltage") + 4), 16);
    Long pvenergytoday = Long.parseLong(result.substring(struct0150.get("pvenergytoday"), struct0150.get("pvenergytoday") + 8), 16);
    Long pvenergytotal = Long.parseLong(result.substring(struct0150.get("pvenergytotal"), struct0150.get("pvenergytotal") + 8), 16);
    int pvtemperature = Integer.valueOf(result.substring(struct0150.get("pvtemperature"), struct0150.get("pvtemperature") + 4), 16);
    int pvipmtemperature = Integer.valueOf(result.substring(struct0150.get("pvipmtemperature"), struct0150.get("pvipmtemperature") + 4), 16);

    System.out.println("pvserial: " + pvserial + "\npvstatus: " + pvstatus + "\npvpowerin: " + pvpowerin + "\npv1voltage: " + pv1voltage
      + "\npv1current: " + pv1current + "\npv1watt: " + pv1watt + "\npv2voltage: " + pv2voltage + "\npv2current: " + pv2current + "\npv2watt: " + pv2watt
      + "\npvpowerout: " + pvpowerout + "\npvfrequentie: " + pvfrequentie + "\npvgridvoltage: " + pvgridvoltage + "\npvenergytoday: " + pvenergytoday
      + "\npvenergytotal: " + pvenergytotal + "\npvtemperature: " + pvtemperature + "\npvipmtemperature: " + pvipmtemperature);

  }
}
