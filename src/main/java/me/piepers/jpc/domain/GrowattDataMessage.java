package me.piepers.jpc.domain;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a data message. Message types in Growatt are indicated by a hexadecimal number. This particular class
 * represents the 0x01 0x04 type as well as the 0x01 0x05 message. The only difference between the two appear to be
 * that 0x01 0x05 also contains a timestamp. This is not really important as the collector
 * ({@link me.piepers.jpc.collector.JpcCollectorVerticle}) also adds a timestamp to the message. It is therefore also
 * not extracted from the message.
 * <p>
 * This instance is also capable of mapping a hexadecimal string to an instance of this class.
 */
@Measurement(name = "yield")
@DataObject
public class GrowattDataMessage implements Jsonable {

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

    @Column
    private final String id;
    @Column(timestamp = true)
    private final Instant received;
    @Column(tag = true)
    private final String pvSerial;
    @Column
    private final PvStatus pvStatus;
    @Column
    private final double pvPowerIn;
    @Column
    private final double pv1Voltage;
    @Column
    private final double pv1Current;
    @Column
    private final double pv1Watt;
    @Column
    private final double pv2Voltage;
    @Column
    private final double pv2Current;
    @Column
    private final double pv2Watt;
    @Column
    private final double pvPowerOut;
    @Column
    private final double pvFrequency;
    @Column
    private final double pvGridVoltage;
    @Column
    private final double pvEnergyToday;
    @Column
    private final double pvEnergyTotal;
    @Column
    private final double pvTemperature;
    @Column
    private final double pvIpmTemperature;


    public GrowattDataMessage(String id, Instant received, String pvSerial, PvStatus pvStatus, double pvPowerIn,
                              double pv1Voltage, double pv1Current, double pv1Watt, double pv2Voltage, double pv2Current,
                              double pv2Watt, double pvPowerOut, double pvFrequency, double pvGridVoltage,
                              double pvEnergyToday, double pvEnergyTotal, double pvTemperature, double pvIpmTemperature) {
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
        this.received = Instant.parse(jsonObject.getString("received"));
        this.pvSerial = jsonObject.getString("pvSerial");
        this.pvStatus = PvStatus.resolve(jsonObject.getString("pvStatus"));
        this.pvPowerIn = jsonObject.getDouble("pvPowerIn");
        this.pv1Voltage = jsonObject.getDouble("pv1Voltage");
        this.pv1Current = jsonObject.getDouble("pv1Current");
        this.pv1Watt = jsonObject.getDouble("pv1Watt");
        this.pv2Voltage = jsonObject.getDouble("pv2Voltage");
        this.pv2Current = jsonObject.getDouble("pv2Current");
        this.pv2Watt = jsonObject.getDouble("pv2Watt");
        this.pvPowerOut = jsonObject.getDouble("pvPowerOut");
        this.pvFrequency = jsonObject.getDouble("pvFrequency");
        this.pvGridVoltage = jsonObject.getDouble("pvGridVoltage");
        this.pvEnergyToday = jsonObject.getDouble("pvEnergyToday");
        this.pvEnergyTotal = jsonObject.getDouble("pvEnergyTotal");
        this.pvTemperature = jsonObject.getDouble("pvTemperature");
        this.pvIpmTemperature = jsonObject.getDouble("pvIpmTemperature");
    }

    /**
     * A mapper that expects an unscrambled hexadecimal String with all the contents for that message.
     *
     * @param dataString, the string with the unscrambled contents of the message.
     * @param id,         the id to take for this message.
     * @param instant,    the timestamp of the message.
     * @return an instance of this object so that it can be processed further.
     */
    public static GrowattDataMessage from(String id, Instant instant, String dataString) {
        // TODO: a more sophisticated way of detecting which function/record it is would be nice.
        Map<String, Integer> struct = dataString.substring(12, 16).equals("0150") ? STRUCT0150 : STRUCT0104;

        String pvserial = dataString.substring(struct.get("pvserial"), struct.get("pvserial") + 20);
        int pvstatus = Integer.valueOf(dataString.substring(struct.get("pvstatus"), struct.get("pvstatus") + 4), 16);
        double pvpowerin = (Long.parseLong(dataString.substring(struct.get("pvpowerin"), struct.get("pvpowerin") + 8), 16)) / 10;
        double pv1voltage = (Integer.valueOf(dataString.substring(struct.get("pv1voltage"), struct.get("pv1voltage") + 4), 16)) / 10;
        double pv1current = (Integer.valueOf(dataString.substring(struct.get("pv1current"), struct.get("pv1current") + 4), 16)) / 10;
        double pv1watt = (Long.parseLong(dataString.substring(struct.get("pv1watt"), struct.get("pv1watt") + 8), 16)) / 10;
        double pv2voltage = (Integer.valueOf(dataString.substring(struct.get("pv2voltage"), struct.get("pv2voltage") + 4), 16)) / 10;
        double pv2current = (Integer.valueOf(dataString.substring(struct.get("pv2current"), struct.get("pv2current") + 4), 16)) / 10;
        double pv2watt = (Long.parseLong(dataString.substring(struct.get("pv2watt"), struct.get("pv2watt") + 8), 16)) / 10;
        double pvpowerout = (Long.parseLong(dataString.substring(struct.get("pvpowerout"), struct.get("pvpowerout") + 8), 16)) / 10;
        double pvfrequency = (Integer.valueOf(dataString.substring(struct.get("pvfrequency"), struct.get("pvfrequency") + 4), 16)) / 10;
        double pvgridvoltage = (Integer.valueOf(dataString.substring(struct.get("pvgridvoltage"), struct.get("pvgridvoltage") + 4), 16)) / 10;
        double pvenergytoday = (Long.parseLong(dataString.substring(struct.get("pvenergytoday"), struct.get("pvenergytoday") + 8), 16)) / 10;
        double pvenergytotal = (Long.parseLong(dataString.substring(struct.get("pvenergytotal"), struct.get("pvenergytotal") + 8), 16)) / 10;
        double pvtemperature = (Integer.valueOf(dataString.substring(struct.get("pvtemperature"), struct.get("pvtemperature") + 4), 16)) / 10;
        double pvipmtemperature = (Integer.valueOf(dataString.substring(struct.get("pvipmtemperature"), struct.get("pvipmtemperature") + 4), 16)) / 10;

        return new GrowattDataMessage(id, instant, pvserial, PvStatus.resolve(pvstatus), pvpowerin,
                pv1voltage, pv1current, pv1watt, pv2voltage, pv2current, pv2watt, pvpowerout, pvfrequency,
                pvgridvoltage, pvenergytoday, pvenergytotal, pvtemperature, pvipmtemperature);
    }

    public String getId() {
        return id;
    }

    public Instant getReceived() {
        return received;
    }

    public String getPvSerial() {
        return pvSerial;
    }

    public PvStatus getPvStatus() {
        return pvStatus;
    }

    public double getPvPowerIn() {
        return pvPowerIn;
    }

    public double getPv1Voltage() {
        return pv1Voltage;
    }

    public double getPv1Current() {
        return pv1Current;
    }

    public double getPv1Watt() {
        return pv1Watt;
    }

    public double getPv2Voltage() {
        return pv2Voltage;
    }

    public double getPv2Current() {
        return pv2Current;
    }

    public double getPv2Watt() {
        return pv2Watt;
    }

    public double getPvPowerOut() {
        return pvPowerOut;
    }

    public double getPvFrequency() {
        return pvFrequency;
    }

    public double getPvGridVoltage() {
        return pvGridVoltage;
    }

    public double getPvEnergyToday() {
        return pvEnergyToday;
    }

    public double getPvEnergyTotal() {
        return pvEnergyTotal;
    }

    public double getPvTemperature() {
        return pvTemperature;
    }

    public double getPvIpmTemperature() {
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
