package me.piepers.jpc.collector;

import io.vertx.core.Handler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.net.NetSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NetSocketConnectionTest {
    @Mock
    Vertx vertx;
    @Mock
    NetSocket netSocket;

    @InjectMocks
    NetSocketConnection netSocketConnection;

    @BeforeEach
    public void init() {
        when(this.netSocket.handler(any(Handler.class))).thenReturn(this.netSocket);
        when(this.netSocket.closeHandler(any(Handler.class))).thenReturn(this.netSocket);
        when(this.netSocket.exceptionHandler(any(Handler.class))).thenReturn(this.netSocket);
    }

    @Test
    public void test_that_when_duration_has_passed_that_connection_is_deemed_stale() {
        // Given
        when(this.netSocket.getDelegate()).thenReturn(mock(io.vertx.core.net.NetSocket.class));
        netSocketConnection = NetSocketConnection.with("1", this.netSocket, this.vertx, "test");

        // When
        var result = netSocketConnection.isStaleConnectionSuspect(Duration.ofMinutes(1), LocalDateTime.now().plusMinutes(2));

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void test_that_when_duration_has_not_passed_that_connection_is_not_deemed_stale() {
        // Given
        when(this.netSocket.getDelegate()).thenReturn(mock(io.vertx.core.net.NetSocket.class));
        netSocketConnection = NetSocketConnection.with("1", this.netSocket, this.vertx, "test");

        // When
        var result = netSocketConnection.isStaleConnectionSuspect(Duration.ofHours(1), LocalDateTime.now());

        // Then
        assertThat(result).isFalse();
    }

    // Tests that if lastActive is somehow after the passed in time that this is not seen as a stale connection.
    @Test
    public void test_that_if_times_are_switched_that_connection_is_not_stale() {
        // Given
        var past = LocalDateTime.now();
        netSocketConnection = new NetSocketConnection("1", this.netSocket, past, past, this.vertx, "test");

        // When
        var result = netSocketConnection.isStaleConnectionSuspect(Duration.ofHours(1), LocalDateTime.now().minusHours(2));

        // Then
        assertThat(result).isFalse();
    }
}
