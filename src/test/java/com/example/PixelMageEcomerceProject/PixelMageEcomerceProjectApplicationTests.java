package com.example.PixelMageEcomerceProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "stripe.api.secret-key=dummy",
    "stripe.api.public-key=dummy",
    "stripe.webhook.secret=dummy",
    "spring.mail.username=dummy",
    "spring.mail.password=dummy"
})
class PixelMageEcomerceProjectApplicationTests {

	@Test
	void contextLoads() {
	}

}
