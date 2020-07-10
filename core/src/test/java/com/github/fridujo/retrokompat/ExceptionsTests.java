package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;

import org.junit.jupiter.api.Test;

class ExceptionsTests {

    @Test
    void areMoreSpecificThan_success() {
        Exceptions lower = new Exceptions(BindException.class);
        Exceptions upper = new Exceptions(BindException.class);

        assertThat(lower.areTheSame(upper)).isTrue();
    }

    @Test
    void areMoreSpecificThan_success_2() {
        Exceptions lower = new Exceptions(BindException.class, ConnectException.class);
        Exceptions upper = new Exceptions(ConnectException.class, BindException.class);

        assertThat(lower.areTheSame(upper)).isTrue();
    }

    @Test
    void areMoreSpecificThan_missing_lower() {
        Exceptions lower = new Exceptions(BindException.class);
        Exceptions upper = new Exceptions(ConnectException.class, BindException.class);

        assertThat(lower.areTheSame(upper)).isFalse();
    }

    @Test
    void areMoreSpecificThan_missing_upper() {
        Exceptions lower = new Exceptions(BindException.class, ConnectException.class, NoRouteToHostException.class);
        Exceptions upper = new Exceptions(ConnectException.class, BindException.class);

        assertThat(lower.areTheSame(upper)).isFalse();
    }

    @Test
    void areMoreSpecificThan_no_match() {
        Exceptions lower = new Exceptions(BindException.class);
        Exceptions upper = new Exceptions(ConnectException.class);

        assertThat(lower.areTheSame(upper)).isFalse();
        assertThat(upper.areTheSame(lower)).isFalse();
    }
}
