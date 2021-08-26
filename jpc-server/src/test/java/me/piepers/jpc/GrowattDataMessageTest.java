package me.piepers.jpc;

import io.vertx.core.json.JsonObject;
import me.piepers.jpc.domain.GrowattDataMessage;
import me.piepers.jpc.domain.PvStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GrowattDataMessageTest {
    @Test
    public void test_that_data_message_can_be_mapped_to_json() {
        // Given
        String expectedId = UUID.randomUUID().toString();
        Instant instant = Instant.now();

        GrowattDataMessage gdm = new GrowattDataMessage(
                expectedId,
                instant,
                "abc",
                PvStatus.NORMAL,
                100.0d,
                500d,
                1234.1d,
                200.12d,
                45.7d,
                1d,
                2d,
                3d,
                4d,
                5d,
                6d,
                6.0,
                7d,
                8d);

        // When
        JsonObject jsonObject = gdm.toJson();

        // Then
        JsonObject expectedJsonObject = new JsonObject()
                .put("id", expectedId)
                .put("received", instant.toString())
                .put("pvSerial", "abc")
                .put("pvStatus", PvStatus.NORMAL)
                .put("pvPowerIn", 100.0d)
                .put("pv1Voltage", 500d)
                .put("pv1Current", 1234.1d)
                .put("pv1Watt", 200.12d)
                .put("pv2Voltage", 45.7d)
                .put("pv2Current", 1d)
                .put("pv2Watt", 2d)
                .put("pvPowerOut", 3d)
                .put("pvFrequency", 4d)
                .put("pvGridVoltage", 5d)
                .put("pvEnergyToday", 6d)
                .put("pvEnergyTotal", 6.0)
                .put("pvTemperature", 7d)
                .put("pvIpmTemperature", 8d);

        assertThat(jsonObject).isEqualTo(expectedJsonObject);

    }

}
