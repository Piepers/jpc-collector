package me.piepers.jpc.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a data message. Message types in Growatt are indicated by a hexadecimal number. This particular class
 * represents the 0x01 0x04 type as well as the 0x01 0x05 message. The only difference between the two appear to be
 * that 0x01 0x05 also contains a timestamp. This is not really important as the collector
 * ({@link me.piepers.jpc.collector.JpcCollectorVerticle}) also adds a timestamp to the message. It is therefore also
 * not extracted from the message.
 * <p>
 * This instance is also capable of mapping a hexadecimal string to an instance of this class.
 */
@DataObject
public class GrowattDataMessage {

    static Map<String, Integer> STRUCT0104 = new HashMap<>() {{
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
    static Map<String, Integer> STRUCT0150 = new HashMap<>() {{
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

    private final String id;
    private final LocalDateTime received;
    private final String pvSerial;
    private final PvStatus pvStatus;
    private final float pvPowerIn;
    private final float pv1Voltage;
    private final float pv1Current;
    private final float pv1Watt;
    private final float pv2Voltage;
    private final float pv2Current;
    private final float pv2Watt;
    private final float pvPowerOut;
    private final float pvFrequency;
    private final float pvGridVoltage;
    private final float pvEnergyToday;
    private final double pvEnergyTotal;
    private final float pvTemperature;
    private final float pvIpmTemperature;


    public GrowattDataMessage(String id, LocalDateTime received, String pvSerial, PvStatus pvStatus, float pvPowerIn,
                              float pv1Voltage, float pv1Current, float pv1Watt, float pv2Voltage, float pv2Current,
                              float pv2Watt, float pvPowerOut, float pvFrequency, float pvGridVoltage,
                              float pvEnergyToday, double pvEnergyTotal, float pvTemperature, float pvIpmTemperature) {
        this.id = id;
        this.received = received;
        this.pvSerial = pvSerial;
        this.pvStatus = pvStatus;
        this.pvPowerIn = pvPowerIn;
        this.pv1Voltage = pv1Voltage;
        this.pv1Current = pv1Current;
        this.pv1Watt = pv1Watt;
        this.pv2Voltage = pv2Voltage;
        this.pv2Current = pv2Current;
        this.pv2Watt = pv2Watt;
        this.pvPowerOut = pvPowerOut;
        this.pvFrequency = pvFrequency;
        this.pvGridVoltage = pvGridVoltage;
        this.pvEnergyToday = pvEnergyToday;
        this.pvEnergyTotal = pvEnergyTotal;
        this.pvTemperature = pvTemperature;
        this.pvIpmTemperature = pvIpmTemperature;
    }

    public GrowattDataMessage(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.received = LocalDateTime.parse(jsonObject.getString("received"));
        this.pvSerial = jsonObject.getString("pvSerial");
        this.pvStatus = PvStatus.resolve(jsonObject.getInteger("status"));
        this.pvPowerIn = jsonObject.getFloat("pvPowerIn");
        this.pv1Voltage = jsonObject.getFloat("pv1Voltage");
        this.pv1Current = jsonObject.getFloat("pv1Current");
        this.pv1Watt = jsonObject.getFloat("pv1Watt");
        this.pv2Voltage = jsonObject.getFloat("pv2Voltage");
        this.pv2Current = jsonObject.getFloat("pv2Current");
        this.pv2Watt = jsonObject.getFloat("pv2Watt");
        this.pvPowerOut = jsonObject.getFloat("pvPowerOut");
        this.pvFrequency = jsonObject.getFloat("pvFrequency");
        this.pvGridVoltage = jsonObject.getFloat("pvGridVoltage");
        this.pvEnergyToday = jsonObject.getFloat("pvEnergyToday");
        this.pvEnergyTotal = jsonObject.getDouble("pvEnergyTotal");
        this.pvTemperature = jsonObject.getFloat("pvTemperature");
        this.pvIpmTemperature = jsonObject.getFloat("pvIpmTemperature");
    }

    /**
     * A mapper that expects an unscrambled hexadecimal String with all the contents for that message.
     *
     * @param dataString,    the string with the unscrambled contents of the message.
     * @param id,            the id to take for this message.
     * @param localDateTime, the timestamp of the message.
     * @return an instance of this object so that it can be processed further.
     */
    public static GrowattDataMessage from(String id, LocalDateTime localDateTime, String dataString) {
        // TODO: a more sophisticated way of detecting which function/record it is would be nice.
        Map<String, Integer> struct = dataString.substring(12, 16).equals("0150") ? STRUCT0150 : STRUCT0104;

        String pvserial = dataString.substring(struct.get("pvserial"), struct.get("pvserial") + 20);
        int pvstatus = Integer.valueOf(dataString.substring(struct.get("pvstatus"), struct.get("pvstatus") + 4), 16);
        float pvpowerin = (Long.parseLong(dataString.substring(struct.get("pvpowerin"), struct.get("pvpowerin") + 8), 16)) / 10;
        float pv1voltage = (Integer.valueOf(dataString.substring(struct.get("pv1voltage"), struct.get("pv1voltage") + 4), 16)) / 10;
        float pv1current = (Integer.valueOf(dataString.substring(struct.get("pv1current"), struct.get("pv1current") + 4), 16)) / 10;
        float pv1watt = (Long.parseLong(dataString.substring(struct.get("pv1watt"), struct.get("pv1watt") + 8), 16)) / 10;
        float pv2voltage = (Integer.valueOf(dataString.substring(struct.get("pv2voltage"), struct.get("pv2voltage") + 4), 16)) / 10;
        float pv2current = (Integer.valueOf(dataString.substring(struct.get("pv2current"), struct.get("pv2current") + 4), 16)) / 10;
        float pv2watt = (Long.parseLong(dataString.substring(struct.get("pv2watt"), struct.get("pv2watt") + 8), 16)) / 10;
        float pvpowerout = (Long.parseLong(dataString.substring(struct.get("pvpowerout"), struct.get("pvpowerout") + 8), 16)) / 10;
        float pvfrequency = (Integer.valueOf(dataString.substring(struct.get("pvfrequency"), struct.get("pvfrequency") + 4), 16)) / 10;
        float pvgridvoltage = (Integer.valueOf(dataString.substring(struct.get("pvgridvoltage"), struct.get("pvgridvoltage") + 4), 16)) / 10;
        float pvenergytoday = (Long.parseLong(dataString.substring(struct.get("pvenergytoday"), struct.get("pvenergytoday") + 8), 16)) / 10;
        double pvenergytotal = (Long.parseLong(dataString.substring(struct.get("pvenergytotal"), struct.get("pvenergytotal") + 8), 16)) / 10;
        float pvtemperature = (Integer.valueOf(dataString.substring(struct.get("pvtemperature"), struct.get("pvtemperature") + 4), 16)) / 10;
        float pvipmtemperature = (Integer.valueOf(dataString.substring(struct.get("pvipmtemperature"), struct.get("pvipmtemperature") + 4), 16)) / 10;

        return new GrowattDataMessage(id, localDateTime, pvserial, PvStatus.resolve(pvstatus), pvpowerin,
                pv1voltage, pv1current, pv1watt, pv2voltage, pv2current, pv2watt, pvpowerout, pvfrequency,
                pvgridvoltage, pvenergytoday, pvenergytotal, pvtemperature, pvipmtemperature);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public String getPvSerial() {
        return pvSerial;
    }

    public PvStatus getPvStatus() {
        return pvStatus;
    }

    public float getPvPowerIn() {
        return pvPowerIn;
    }

    public float getPv1Voltage() {
        return pv1Voltage;
    }

    public float getPv1Current() {
        return pv1Current;
    }

    public float getPv1Watt() {
        return pv1Watt;
    }

    public float getPv2Voltage() {
        return pv2Voltage;
    }

    public float getPv2Current() {
        return pv2Current;
    }

    public float getPv2Watt() {
        return pv2Watt;
    }

    public float getPvPowerOut() {
        return pvPowerOut;
    }

    public float getPvFrequency() {
        return pvFrequency;
    }

    public float getPvGridVoltage() {
        return pvGridVoltage;
    }

    public float getPvEnergyToday() {
        return pvEnergyToday;
    }

    public double getPvEnergyTotal() {
        return pvEnergyTotal;
    }

    public float getPvTemperature() {
        return pvTemperature;
    }

    public float getIpmTemperature() {
        return pvIpmTemperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GrowattDataMessage that = (GrowattDataMessage) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String asFormattedString(String hexString) {
        return "hex decrypted: " + hexString + "\nuuid: " + id + "\ndate/time: " + received + "\npvserial: " + pvSerial +
                "\npvstatus: " + pvStatus.getDescription() + "\npvpowerin: " + pvPowerIn + "\npv1voltage: " + pv1Voltage +
                "\npv1current: " + pv1Current + "\npv1watt: " + pv1Watt + "\npv2voltage: " + pv2Voltage +
                "\npv2current: " + pv2Current + "\npv2watt: " + pv2Watt + "\npvpowerout: " + pvPowerOut +
                "\npvfrequency: " + pvFrequency + "\npvgridvoltage: " + pvGridVoltage + "\npvenergytoday: " + pvEnergyToday +
                "\npvenergytotal: " + pvEnergyTotal + "\npvtemperature: " + pvTemperature +
                "\npvipmtemperature: " + pvIpmTemperature;
    }

    @Override
    public String toString() {
        return "GrowattDataMessage{" +
                "id='" + id + '\'' +
                ", received=" + received +
                ", pvSerial='" + pvSerial + '\'' +
                ", pvStatus=" + pvStatus +
                ", pvPowerIn=" + pvPowerIn +
                ", pv1Voltage=" + pv1Voltage +
                ", pv1Current=" + pv1Current +
                ", pv1Watt=" + pv1Watt +
                ", pv2Voltage=" + pv2Voltage +
                ", pv2Current=" + pv2Current +
                ", pv2Watt=" + pv2Watt +
                ", pvPowerOut=" + pvPowerOut +
                ", pvFrequency=" + pvFrequency +
                ", pvGridVoltage=" + pvGridVoltage +
                ", pvEnergyToday=" + pvEnergyToday +
                ", pvEnergyTotal=" + pvEnergyTotal +
                ", pvTemperature=" + pvTemperature +
                ", ipmTemperature=" + pvIpmTemperature +
                '}';
    }
}
