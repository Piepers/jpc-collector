package me.piepers.jpc.collector;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import me.piepers.jpc.domain.PvStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

@Disabled
public class InfluxDbClientTest {

    @Test
    public void insert_test_data() {
//        String token = "UMw-JTMQZAQk0PmhXZFQnRht5wd8tBjxnR9waRmStHDqZbdwvLiZm364xipSj0uViScoHLXqlNKpfUd1xns6ng==";
        String token = "wRWNx9hg_BxIknQpWlgQ9-tk6-fkze61_8e357leAf6rM2b1Bs1vZgu93Qd71PfG73i78aQP16JNkHjITpWhpw==";
        String bucket = "sandbox";
        String org = "piepers";


        InfluxDBClient client = InfluxDBClientFactory.create("http://192.168.188.3:8086", token.toCharArray());

        // Generate instants for each minute of the day of yesterday
        Instant i = Instant.now().minusMillis(3_600_000 * 24);
        for (long c = i.toEpochMilli(); c < Instant.now().toEpochMilli(); c += 60_000) {
            Instant in = Instant.ofEpochMilli(c);
            Double number = Math.random() * 100;
            TestMeasurement tm = new TestMeasurement(UUID.randomUUID().toString(), in, "test-serial-1",
                    PvStatus.NORMAL, number, number, number, number, number, number, number, number, number, number,
                    number, number, number, number);
//            System.out.println("Adding " + in);
            try (WriteApi writeApi = client.getWriteApi()) {
                writeApi.writeMeasurement(bucket, org, WritePrecision.S, tm);
            }
        }

        client.close();
    }

}
