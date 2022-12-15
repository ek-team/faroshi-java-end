package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.service.RedisService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * redis 数据
 */
@RestController
@AllArgsConstructor
@RequestMapping("/redis")
public class RedisController {
	private final RedisService redisService;

	/**
	 * 查询redis信息
	 *
	 * @return
	 */
	@GetMapping("/info")
	public RestResponse memory() {
		return RestResponse.ok(redisService.getInfo());
	}

}
