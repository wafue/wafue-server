/**
 * Copyright (c) 2015-2016, Chill Zhuang 庄骞 (smallchill@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smallchill.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.common.base.BaseController;
import com.smallchill.core.constant.ConstShiro;
import com.smallchill.core.shiro.ShiroKit;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Maps;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.kit.CacheKit;
import com.smallchill.core.toolbox.kit.JsonKit;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.system.meta.intercept.RoleIntercept;
import com.smallchill.system.model.Role;
import com.smallchill.system.service.RoleService;

@Controller
@RequestMapping("/role")
public class RoleController extends BaseController{
	private static String LIST_SOURCE = "Role.list";
	private static String BASE_PATH = "/system/role/";
	private static String CODE = "role";
	private static String PERFIX = "tfw_role";
	
	@Autowired
	RoleService service;

	@RequestMapping("/")
	public ModelAndView index() {
		ModelAndView view = new ModelAndView(BASE_PATH + "role.html");
		view.addObject("code", CODE);
		return view;
	}
	
	@ResponseBody
	@RequestMapping(KEY_LIST)
	public Object list() {
		Object gird = paginate(LIST_SOURCE, new RoleIntercept());
		return gird;
	}
	
	@RequestMapping(KEY_ADD)
	public ModelAndView add() {
		ModelAndView view = new ModelAndView(BASE_PATH + "role_add.html");
		view.addObject("code", CODE);
		return view;
	}
	
	@RequestMapping(KEY_ADD + "/{id}")
	public ModelAndView add(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "role_add.html");
		if (StrKit.notBlank(id)) {
			view.addObject("pId", id);
			view.addObject("num", service.findLastNum(id));
		}
		view.addObject("code", CODE);
		return view;
	}
	
	@RequestMapping(KEY_EDIT + "/{id}")
	public ModelAndView edit(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "role_edit.html");
		Role role = service.findById(id);
		view.addObject("model", JsonKit.toJson(role));
		view.addObject("code", CODE);
		return view;
	}

	@RequestMapping(KEY_VIEW + "/{id}")
	public ModelAndView view(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "role_view.html");
		Role role = service.findById(id);
		Role parent = service.findById(role.getPid());
		String pName = (null == parent) ? "" : parent.getName();
		Maps maps = Maps.parse(role);
		maps.set("deptName", Func.getDeptName(role.getDeptid()))
			.set("pName", pName);
		view.addObject("model", JsonKit.toJson(maps));
		view.addObject("code", CODE);
		return view;
	}
	
	@RequestMapping("/authority/{roleId}/{roleName}")
	public ModelAndView authority(@PathVariable String roleId, @PathVariable String roleName) {
		ModelAndView view = new ModelAndView(BASE_PATH + "role_authority.html");	
		if(!ShiroKit.hasAnyRoles(ConstShiro.ADMINISTRATOR + "," + ConstShiro.ADMIN)){
			view.setViewName("redirect:/unauth");
			return view;
		}
		view.addObject("roleId", roleId);
		view.addObject("roleName", Func.decodeUrl(roleName));
		return view;
	}
	
	@ResponseBody
	@RequestMapping("/saveAuthority")
	public AjaxResult saveAuthority(@RequestParam String ids, @RequestParam String roleId) {
		String[] id = ids.split(",");
		if (id.length <= 1) {
			CacheKit.removeAll(ROLE_CACHE);
			CacheKit.removeAll(MENU_CACHE);
			return success("设置成功");
		}
		boolean temp = service.saveAuthority(ids, roleId);
		if (temp) {
			CacheKit.removeAll(ROLE_CACHE);
			CacheKit.removeAll(MENU_CACHE);
			return success("设置成功");
		} else {
			return error("设置失败");
		}
	}
	
	@ResponseBody
	@RequestMapping(KEY_SAVE)
	public AjaxResult save() {
		Role role = mapping(PERFIX, Role.class);
		boolean temp = service.save(role);
		if (temp) {
			CacheKit.removeAll(ROLE_CACHE);
			CacheKit.removeAll(MENU_CACHE);
			return success(SAVE_SUCCESS_MSG);
		} else {
			return error(SAVE_FAIL_MSG);
		}
	}
	
	@ResponseBody
	@RequestMapping(KEY_UPDATE)
	public AjaxResult update() {
		Role role = mapping(PERFIX, Role.class);
		role.setVersion(getParaToInt("VERSION", 0) + 1);
		boolean temp = service.update(role);
		if (temp) {
			CacheKit.removeAll(ROLE_CACHE);
			CacheKit.removeAll(MENU_CACHE);
			return success(UPDATE_SUCCESS_MSG);
		} else {
			return error(UPDATE_FAIL_MSG);
		}
	}
	
	@ResponseBody
	@RequestMapping(KEY_REMOVE)
	public AjaxResult remove(@RequestParam String ids) {
		int cnt = service.deleteByIds(ids);
		if (cnt > 0) {
			CacheKit.removeAll(ROLE_CACHE);
			CacheKit.removeAll(MENU_CACHE);
			return success(DEL_SUCCESS_MSG);
		} else {
			return error(DEL_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping("/getPowerById")
	public AjaxResult getPowerById(@RequestParam String id) {
		int cnt = service.getParentCnt(id);
		if (cnt > 0) {
			return success("success");
		} else {
			return error("error");
		}
	}
	
}