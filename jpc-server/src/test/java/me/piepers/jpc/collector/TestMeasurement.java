package me.piepers.jpc.collector;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import me.piepers.jpc.domain.Jsonable;
import me.piepers.jpc.domain.PvStatus;

import java.time.Instant;

@Measurement(name = "yield")
@DataObject
public class TestMeasurement implements Jsonable {

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


    public TestMeasurement(String id, Instant received, String pvSerial, PvStatus pvStatus, double pvPowerIn,
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

    public TestMeasurement(JsonObject jsonObject) {
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

        TestMeasurement that = (TestMeasurement) o;

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
        return "TestMeasurement{" +
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
