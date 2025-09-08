package com.tsc.zappy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tsc.zappy.components.HardwareInfo;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
class ZappyApplicationTests {

	private HardwareInfo hardwareInfo;

	@Autowired
	public ZappyApplicationTests(HardwareInfo hardwareInfo) {
		this.hardwareInfo = hardwareInfo;
	}
	
	@Test
	void testHmac() {
		hardwareInfo.getHostName();
	}

}
