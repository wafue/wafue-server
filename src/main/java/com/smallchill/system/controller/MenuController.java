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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.common.base.BaseController;
import com.smallchill.core.annotation.Before;
import com.smallchill.core.annotation.Permission;
import com.smallchill.core.constant.ConstCache;
import com.smallchill.core.constant.ConstShiro;
import com.smallchill.core.interfaces.ILoader;
import com.smallchill.core.plugins.dao.Db;
import com.smallchill.core.shiro.ShiroKit;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Maps;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.kit.CacheKit;
import com.smallchill.core.toolbox.kit.JsonKit;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.system.meta.intercept.MenuValidator;
import com.smallchill.system.model.Menu;
import com.smallchill.system.service.MenuService;

@Controller
@RequestMapping("/menu")
public class MenuController extends BaseController implements ConstShiro{
	private static String LIST_SOURCE = "Menu.list";
	private static String BASE_PATH = "/system/menu/";
	private static String CODE = "menu";
	private static String PERFIX = "TFW_MENU";

	@Autowired
	MenuService service;

	@RequestMapping("/")
	@Permission(ADMINISTRATOR)
	public ModelAndView index() {
		ModelAndView view = new ModelAndView(BASE_PATH + "menu.html");
		view.addObject("code", CODE);
		return view;
	}

	@RequestMapping(KEY_ADD)
	public ModelAndView add() {
		ModelAndView view = new ModelAndView(BASE_PATH + "menu_add.html");
		if(ShiroKit.lacksRole(ADMINISTRATOR)){
			view.setViewName(REDIRECT_UNAUTH);
			return view;
		}
		view.addObject("code", CODE);
		return view;
	}
	
	@RequestMapping(KEY_ADD + "/{id}")
	@Permission(ADMINISTRATOR)
	public ModelAndView add(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "menu_add.html");
		if (StrKit.notBlank(id)) {
			Menu menu = service.findById(id);
			view.addObject("PCODE", menu.getCode());
			view.addObject("LEVELS", menu.getLevels() + 1);
			view.addObject("NUM", service.findLastNum(menu.getCode()));
		}
		view.addObject("code", CODE);
		return view;
	}

	@RequestMapping(KEY_EDIT + "/{id}")
	@Permission(ADMINISTRATOR)
	public ModelAndView edit(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "menu_edit.html");
		Menu menu = service.findById(id);
		view.addObject("model", JsonKit.toJson(menu));
		view.addObject("code", CODE);
		return view;
	}

	@RequestMapping(KEY_VIEW + "/{id}")
	@Permission(ADMINISTRATOR)
	public ModelAndView view(@PathVariable String id) {
		ModelAndView view = new ModelAndView(BASE_PATH + "menu_view.html");
		Menu menu = service.findById(id);
		view.addObject("model", JsonKit.toJson(menu));
		view.addObject("code", CODE);
		return view;
	}

	@ResponseBody
	@RequestMapping(KEY_LIST)
	@Permission(ADMINISTRATOR)
	public Object list() {
		Object gird = paginate(LIST_SOURCE);
		return gird;
	}

	@ResponseBody
	@Before(MenuValidator.class)
	@RequestMapping(KEY_SAVE)
	@Permission(ADMINISTRATOR)
	public AjaxResult save() {
		Menu menu = mapping(PERFIX, Menu.class);
		menu.setStatus(1);
		boolean temp = service.save(menu);
		if (temp) {
			CacheKit.removeAll(MENU_CACHE);
			return success(SAVE_SUCCESS_MSG);
		} else {
			return error(SAVE_FAIL_MSG);
		}
	}

	@ResponseBody
	@Before(MenuValidator.class)
	@RequestMapping(KEY_UPDATE)
	@Permission(ADMINISTRATOR)
	public AjaxResult update() {
		Menu menu = mapping(PERFIX, Menu.class);
		menu.setVersion(getParaToInt("VERSION", 0) + 1);
		boolean temp = service.update(menu);
		if (temp) {
			if(Func.equals(menu.getUrl(), "null")){
				service.updateBy("url = ''", "id = #{id}", Maps.create().set("id", menu.getId()));
			}
			CacheKit.removeAll(MENU_CACHE);
			return success(UPDATE_SUCCESS_MSG);
		} else {
			return error(UPDATE_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping(KEY_DEL)
	@Permission(ADMINISTRATOR)
	public AjaxResult del(@RequestParam String ids) {
		boolean temp = service.updateStatus(ids, 2);
		if (temp) {
			CacheKit.removeAll(MENU_CACHE);
			return success(DEL_SUCCESS_MSG);
		} else {
			return error(DEL_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping(KEY_RESTORE)
	@Permission(ADMINISTRATOR)
	public AjaxResult restore(@RequestParam String ids) {
		boolean temp = service.updateStatus(ids, 1);
		if (temp) {
			CacheKit.removeAll(MENU_CACHE);
			return success(RESTORE_SUCCESS_MSG);
		} else {
			return error(RESTORE_FAIL_MSG);
		}

	}

	@ResponseBody
	@RequestMapping(KEY_REMOVE)
	@Permission(ADMINISTRATOR)
	public AjaxResult remove(@RequestParam String ids) {
		int cnt = service.deleteByIds(ids);
		if (cnt > 0) {
			CacheKit.removeAll(MENU_CACHE);
			return success(DEL_SUCCESS_MSG);
		} else {
			return error(DEL_FAIL_MSG);
		}
	}
	
	
	
	@ResponseBody
	@RequestMapping("/getMenu")
	public List<Map<String, Object>> getMenu(){
		String MENU_CACHE = ConstCache.MENU_CACHE;
		final Object userId = getPara("userId");
		final Object roleId = getPara("roleId");

		Map<String, Object> userRole = CacheKit.get(MENU_CACHE, "role_ext_" + userId, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectOne("select * from TFW_ROLE_EXT where USERID=#{userId}", Maps.create().set("userId", userId));
			}
		}); 


		String roleIn = "0";
		String roleOut = "0";
		if (!Func.isEmpty(userRole)) {
			Maps map = Maps.parse(userRole);
			roleIn = map.getStr("ROLEIN");
			roleOut = map.getStr("ROLEOUT");
		}
		final StringBuilder sql = new StringBuilder();
		sql.append("select * from TFW_MENU  ");
		sql.append(" where ( ");
		sql.append("	 (status=1)");
		sql.append("	 and (icon is not null and icon not LIKE '%btn%' and icon not LIKE '%icon%' ) ");
		sql.append("	 and (id in (select menuId from TFW_RELATION where roleId in (" + roleId + ")) or id in (" + roleIn + "))");
		sql.append("	 and id not in(" + roleOut + ")");
		sql.append("	)");
		sql.append(" order by levels,pCode,num");

		List<Map<String, Object>> sideBar = CacheKit.get(MENU_CACHE, "sideBar_" + userId, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectList(sql.toString());
			}
		}); 
		return sideBar;
	}

}