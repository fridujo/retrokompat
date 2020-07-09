package com;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SampleTest {

    @Test
    void sample_test() {
        assertThat(new Sample().db(2)).isEqualTo(4);
    }
}
