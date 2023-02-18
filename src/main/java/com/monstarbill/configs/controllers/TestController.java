package com.monstarbill.configs.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.feingclients.MasterServiceClient;

//import feign.FeignException;

@RestController
@RequestMapping("/setup")
public class TestController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Environment environment;
	
//	@Autowired
//	private RestTemplate restTemplate;

	@Autowired
	private MasterServiceClient masterServiceClient; 

	@GetMapping("/status/check")
	public String getStatus() {
		return "working good from setup..." + environment.getProperty("local.server.port");
	}
	
	@GetMapping("/status/check-comm")
	public ResponseEntity<List<String>> getStatusComm() {
		// 1. Communication using REST Template
//		ResponseEntity<List<String>> data = restTemplate.exchange("http://masters-ws/masters/status/comm", HttpMethod.GET, null, 
//				new ParameterizedTypeReference<List<String>>() {});
		
		// 2. Communication using Feign Client (Declarative approach)
		List<String> data = new ArrayList<String>();
//		try {
			data = this.masterServiceClient.getStatusList(1, "aaa");
//		} catch (FeignException e) {
//			logger.error(e.getLocalizedMessage());
//		}
		return new ResponseEntity<List<String>>(data, HttpStatus.OK);
	}
	
	@GetMapping("/status/comm")
	public List<String> getStatusComm(@RequestParam int id, @RequestParam String name) {
		List<String> list = new ArrayList<String>();
		list.add("aaaa");
		list.add("bbb");
		list.add("ccc");
		list.add("ddddd");
		list.add("eee");
		return list;
	}
}
