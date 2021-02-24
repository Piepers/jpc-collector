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
                100.0f,
                500f,
                1234.1f,
                200.12f,
                45.7f,
                1f,
                2f,
                3f,
                4f,
                5f,
                6f,
                6.0,
                7f,
                8f);

        // When
        JsonObject jsonObject = gdm.toJson();

        // Then
        JsonObject expectedJsonObject = new JsonObject()
                .put("id", expectedId)
                .put("received", instant.toString())
                .put("pvSerial", "abc")
                .put("pvStatus", PvStatus.NORMAL)
                .put("pvPowerIn", 100.0f)
                .put("pv1Voltage", 500f)
                .put("pv1Current", 1234.1f)
                .put("pv1Watt", 200.12f)
                .put("pv2Voltage", 45.7f)
                .put("pv2Current", 1f)
                .put("pv2Watt", 2f)
                .put("pvPowerOut", 3f)
                .put("pvFrequency", 4f)
                .put("pvGridVoltage", 5f)
                .put("pvEnergyToday", 6f)
                .put("pvEnergyTotal", 6.0)
                .put("pvTemperature", 7f)
                .put("pvIpmTemperature", 8f);

        assertThat(jsonObject).isEqualTo(expectedJsonObject);

    }

}
