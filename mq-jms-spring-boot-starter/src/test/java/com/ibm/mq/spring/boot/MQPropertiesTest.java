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
        "ibm.mq.queueManager=QMSET",
        "ibm.mq.channel=CHANNELSET",
        "ibm.mq.connName=CONNSET",
        "ibm.mq.user=USER",
        "ibm.mq.password=PASS",
        "ibm.mq.clientId=mqm",
        "ibm.mq.applicationName=MQSpringTest",
        "ibm.mq.useIBMCipherMappings=true",
        "ibm.mq.userAuthenticationMQCSP=true",
        "ibm.mq.sslCipherSuite=CIPHER_SUITE",
        "ibm.mq.sslCipherSpec=CIPHER_SPEC",
        "ibm.mq.sslPeerName=CN=Mark,OU=IBM,C=GB",
        "ibm.mq.ccdtUrl=file:///home/admdata/ccdt1.tab"
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
        assertThat(properties.getClientId()).isEqualTo("mqm");
        assertThat(properties.getApplicationName()).isEqualTo("MQSpringTest");
        assertThat(properties.isUseIBMCipherMappings()).isEqualTo(true);
        assertThat(properties.isUserAuthenticationMQCSP()).isEqualTo(true);
        assertThat(System.getProperty("com.ibm.mq.cfg.useIBMCipherMappings")).isEqualTo("true");
        assertThat(properties.getSslCipherSuite()).isEqualTo("CIPHER_SUITE");
        assertThat(properties.getSslCipherSpec()).isEqualTo("CIPHER_SPEC");
        assertThat(properties.getSslPeerName()).isEqualTo("CN=Mark,OU=IBM,C=GB");
        assertThat(properties.getCcdtUrl()).isEqualTo("file:///home/admdata/ccdt1.tab");
    }
}
