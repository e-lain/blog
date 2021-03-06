package top.zerotop.blog.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import top.zerotop.blog.releam.CustomToken;
import top.zerotop.blog.service.UserRoleService;
import top.zerotop.blog.service.UserService;
import top.zerotop.utils.ServiceResult;
import top.zerotop.blog.web.Request.AdminRequest;
import top.zerotop.global.exception.BlogException;

import javax.servlet.http.HttpServletRequest;

import static top.zerotop.global.enums.UserRoleEnum.ADMIN;

/**
 * @author 作者: zerotop
 * @createDate 创建时间: 2018年5月31日下午11:29:14
 */
@Api(value = "管理员请求接口", description = "管理员请求接口")
@RestController
@RequestMapping(value = "/api/admin/v1/account", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class AccountController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private UserService userService;
    @Autowired
    UserRoleService userRoleService;

    @ApiOperation(value = "超级管理员添加管理员", notes = "超级管理员添加管理员")
    @PostMapping(value = "/admin/user/register")
    public ServiceResult insertAdmin(@ApiParam(value = "提供注册信息")
                                     @RequestBody AdminRequest adminRequest) throws BlogException {
        Assert.isTrue(adminRequest != null && StringUtils.hasText(adminRequest.getUsername())
                && StringUtils.hasText(adminRequest.getPassword()), "注册信息缺失");

        userService.insertAdmin(adminRequest);
        return ServiceResult.SUCCESS;
    }

    @ApiOperation(value = "管理员通过用户名获取用户详细信息")
    @PostMapping(value = "/user/select")
    public ServiceResult selectAdmin(@ApiParam(value = "登录时提供用户名", required = true)
                                     @RequestParam String username) {
        Assert.notNull(username, "用户名不可为空");

        return ServiceResult.make(userService.selectAdminByUserName(username));
    }


    @ApiOperation(value = "管理员登录", notes = "只有管理员能登录")
    @PostMapping(value = "/admin/login")
    public ServiceResult adminLogin(@ApiParam(value = "登录时提供用户名", required = true)
                                    @RequestParam String username,
                                    @ApiParam(value = "登录时提供密码", required = true)
                                    @RequestParam String password,
                                    HttpServletRequest req) {
        Assert.isTrue(StringUtils.hasText(username) && StringUtils.hasText(password), "用户名或密码不能为空");

        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated() && subject.getPrincipal().equals(username)) {
            return ServiceResult.error(400, "当前用户已登录");
        }

        logger.info(String.format("user:{%s} login.", username));
        CustomToken token = new CustomToken(username, password, ADMIN.getRoleType());
        try {
            token.setRememberMe(true);
            subject.login(token);
        } catch (IncorrectCredentialsException | UnknownAccountException ice) {
            return ServiceResult.error(400, "用户名或密码错误");
        } catch (ExcessiveAttemptsException eae) {
            return ServiceResult.error(400, "请稍后尝试");
        }
        req.getSession().setAttribute("user", username);
        return ServiceResult.SUCCESS;
    }

    /**
     * 管理员登出
     *
     * @return
     */
    @ApiOperation(value = "管理员登出", notes = "管理员登出接口")
    @GetMapping(value = "/admin/logout")
    public ServiceResult adminLoginOut() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return ServiceResult.SUCCESS;
    }

}
