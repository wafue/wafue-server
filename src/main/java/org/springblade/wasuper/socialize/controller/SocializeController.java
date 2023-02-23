package org.springblade.wasuper.socialize.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 首页
 *
 * @author zhuangqian
 */
@RestController
@RequestMapping("/blade-socialize")
@AllArgsConstructor
@ApiSort(1)
@Api(value = "社交接口", tags = "社交接口")
public class SocializeController extends BladeController implements CacheNames {

	/**
	 * 点赞
	 *
	 * @return
	 */
	@GetMapping("/likes")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "查看数据列表")
	public R list() {

		return R.data("点赞");
	}
}
