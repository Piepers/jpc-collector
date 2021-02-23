package me.piepers.jpc.client;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import me.piepers.jpc.domain.Jsonable;

@DataObject
public class InfluxConfig implements Jsonable {
    private final String token;
    private final String bucketName;
    private final String organization;
    private final String host;
    private final int port;

    public InfluxConfig(String token, String bucketName, String organization, String host, int port) {
        this.token = token;
        this.bucketName = bucketName;
        this.organization = organization;
        this.host = host;
        this.port = port;
    }

    public InfluxConfig(JsonObject jsonObject) {
        this.token = jsonObject.getString("token");
        this.bucketName = jsonObject.getString("bucketName");
        this.organization = jsonObject.getString("organization");
        this.host = jsonObject.getString("host");
        this.port = jsonObject.getInteger("port");
    }

    public static InfluxConfig from(JsonObject config) {
        JsonObject influxConfig = config.getJsonObject("influx-client");
        var token = influxConfig.getString("token");
        var bucketName = influxConfig.getString("bucketName");
        var organization = influxConfig.getString("organization");
        var host = influxConfig.getString("host");
        var port = influxConfig.getInteger("port");
        return new InfluxConfig(token, bucketName, organization, host, port);
    }

    public String getToken() {
        return token;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InfluxConfig that = (InfluxConfig) o;

        if (port != that.port) return false;
        if (!token.equals(that.token)) return false;
        if (!bucketName.equals(that.bucketName)) return false;
        if (!organization.equals(that.organization)) return false;
        return host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + bucketName.hashCode();
        result = 31 * result + organization.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "InfluxConfig{" +
                "token='" + token + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", organization='" + organization + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
