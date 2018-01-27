package com.hzdz.ls.controller;

import com.hzdz.ls.common.Result;
import com.hzdz.ls.service.SystemManagerServer;
import com.hzdz.ls.service.module.CloudPhotographyServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@CrossOrigin(value = "*", maxAge = 3600)
@RequestMapping("/back/systemManager")
@Api(value = "管理员相关接口")
public class SystemManagerController {

    @Autowired
    private SystemManagerServer systemManagerServer;

    @Autowired
    private CloudPhotographyServer cloudPhotographyServer;

    /***
     * 新增管理员
     * @param userAccount
     * @param password
     * @param managerType
     * @param request
     * @return
     */
    @RequestMapping(value = "/addNewManager", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "新增管理员", httpMethod = "POST")
    public Result addNewManager(@RequestParam String userAccount,
                                @RequestParam String password,
                                @ApiParam(name = "managerType", value = "管理员类型： 0 普通管理员， 1 超级管理员")@RequestParam Integer managerType,
                                @RequestParam String remarks,
                                HttpServletRequest request){
        return systemManagerServer.addNewManager(userAccount, password, managerType, remarks, request);
    }

    /**
     * 更改密码
     * @param password
     * @param request
     * @return
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更改密码", httpMethod = "POST")
    public Result updatePassword(@RequestParam Integer id, @RequestParam String password, @RequestParam String oldPassword, HttpServletRequest request){
        return systemManagerServer.updatePassword(id, password, oldPassword, request);
    }

    /**
     * 冻结管理员
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/frozenManager", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "冻结管理员", httpMethod = "POST")
    public Result frozenManager(@RequestParam Integer id, HttpServletRequest request){
        return systemManagerServer.frozenManager(id, request);
    }

    /**
     * 管理员解冻
     * @param id 管理员id
     * @param request 请求
     * @return 返回操作结果
     */
    @RequestMapping(value = "/thawManager", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "解冻管理员", httpMethod = "POST")
    public Result thawManager(@RequestParam Integer id, HttpServletRequest request){
        return systemManagerServer.thawManager(id, request);
    }

    /**
     * 重置密码
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "重置密码", httpMethod = "POST")
    public Result resetPassword(@RequestParam Integer id, HttpServletRequest request){
        return systemManagerServer.resetPassword(id, request);
    }

    /**
     * 查询所有管理员（不含超管）
     * @param request
     * @return
     */
    @RequestMapping("/queryAllManager")
    @ResponseBody
    @ApiOperation(value = "查询所有管理员（不含超管）", httpMethod = "POST")
    public Result selectAllManager(@RequestParam(required = false) Integer id,
                                   @RequestParam(required = false) String userAccount,
                                   @ApiParam(required = false, name = "frozen", value = "是否冻结（1：冻结；0：不冻结）")@RequestParam(required = false) Integer frozen,
                                   HttpServletRequest request){
        return systemManagerServer.selectAllManager(id, userAccount, frozen, request);
    }

    /**
     * 登录验证
     * @param userAccount
     * @param password
     * @param request
     * @return
     */
    @RequestMapping(value = "/loginVerify", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "登录验证", httpMethod = "POST")
    public Result loginVerify(@RequestParam String userAccount,
                              @RequestParam String password,
                              HttpServletRequest request){
        return systemManagerServer.loginVerify(userAccount, password, request);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "退出登录", httpMethod = "POST")
    public Result logout(HttpServletRequest request){
        return systemManagerServer.logout(request);
    }





}

