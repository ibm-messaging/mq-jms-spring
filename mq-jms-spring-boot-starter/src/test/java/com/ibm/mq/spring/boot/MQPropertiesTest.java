package com.ibm.mq.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={MQAutoConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "mq.queueManager=QMSET",
        "mq.channel=CHANNELSET",
        "mq.connName=CONNSET",
        "mq.user=USER",
        "mq.password=PASS",
        "mq.useIBMCipherMappings=true",
        "mq.sslCipherSuite=CIPHER_SUITE",
        "mq.sslCipherSpec=CIPHER_SPEC"
})
public class MQPropertiesTest {

    @Autowired
    MQConfigurationProperties properties;

    @Test
    public void test() {
        assertThat(properties.getQueueManager()).isEqualTo("QMSET");
        assertThat(properties.getChannel()).isEqualTo("CHANNELSET");
        assertThat(properties.getConnName()).isEqualTo("CONNSET");
        assertThat(properties.getUser()).isEqualTo("USER");
        assertThat(properties.getPassword()).isEqualTo("PASS");
        assertThat(properties.isUseIBMCipherMappings()).isEqualTo(true);
        assertThat(System.getProperty("com.ibm.mq.cfg.useIBMCipherMappings")).isEqualTo("true");
        assertThat(properties.getSslCipherSuite()).isEqualTo("CIPHER_SUITE");
        assertThat(properties.getSslCipherSpec()).isEqualTo("CIPHER_SPEC");
    }
}
