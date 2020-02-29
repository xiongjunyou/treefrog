package com.shuwa.treefrog.web;

import com.github.pagehelper.PageInfo;
import com.shuwa.treefrog.entity.User;
import com.shuwa.treefrog.exception.LoginException;
import com.shuwa.treefrog.exception.RegisterException;
import com.shuwa.treefrog.model.PageParam;
import com.shuwa.treefrog.service.impl.AdminService;
import com.shuwa.treefrog.service.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于转发 管理员 相关页面
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UserService userService; // userervice -> userService
    @Autowired
    private AdminService adminService;

    /**
     * 进入登录界面
     * @return
     */
    @GetMapping
    public String toLoginPage(){
        return "admin/login";
    }

    /**
     * 传递到主页面
     * 主页面
     * @return
     */
    @RequestMapping(value = "/user_list")
    public String userList() {
        logger.info("AdminController->userList");
        return "admin/list";
    }

    /**
     * 删除用户
     *
     * @param id
     * @return
     */
    @DeleteMapping("/userDelete/{id}")
    public String userDelete(@PathVariable("id") int id) {
        logger.info("AdminController->userDelete");
        //判断用户权限，如果不是管理员，则显示不能删除
        userService.deleteUser(id);
        return "admin/list";
    }

    /**
     * 传递到 users 页面
     *
     * @return
     */
    @GetMapping(value = "/users")
    public String users(Model model) {
        logger.info("AdminController->users");
        List<User> users = userService.getAllUser();
        model.addAttribute("users", users);
        return "admin/list";
    }

    /**
     * 处理 管理员登陆
     * @return
     */
    @PostMapping(value = "/adminLoginForm")
    public String adminLoginForm(
            @RequestParam("username") String userName
            ,@RequestParam("password") String password
            ,HttpServletRequest request) {
        logger.info("AdminController->adminLoginForm");
        Map<String,String> loginErrorMap = new HashMap<>();
        try {
            adminService.login(userName,password);
        } catch (LoginException e) {
            loginErrorMap.put("loginError",e.getMessage());
            request.setAttribute("loginErrorMap", loginErrorMap);
            return "admin/login";
        }
        return "admin/list"; //登录成功，到用户管理页面 list.html
    }

    @PostMapping(value = "/adminRegisterForm")
    public String adminRegisterForm(
            @RequestParam("username") String userName
            , @RequestParam("password") String password
            , @RequestParam("rePassword") String rePassword
            ,Model model
            ) {
        logger.info("AdminController->adminRegisterForm");
        Map<String,String> registerErrorMap = new HashMap<>();
        //进行简单的密码重复验证
        if (password != null && !password.equals(rePassword)) {
           model.addAttribute("passwordmsg","两次密码不匹配！");
            return "admin/login";
        }
        try {
            adminService.register(userName,password);
        } catch (RegisterException e) {
            logger.info(e.toString());
            model.addAttribute("usernamemsg","用户名重复！");
            return "admin/login";
        }
//        注册成功后重定向的登录页面，否则再次注册转调路径会出错
        return "redirect:/admin";
    }

    /**
     * 管理员进行用户分页查询功能
     * @param currentPage
     * @param model
     * @return
     */
    @GetMapping(value = "/listUser/{id}")
    public String userList(@PathVariable("id") Integer currentPage, Model model) {
        int limit = 6; //页面数据个数
        PageInfo<User> pageInfo = adminService.getAllUserByPageingQuery(currentPage,limit);
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageInfo.getPageNum());
        pageParam.setPageTotal(pageInfo.getTotal());
        pageParam.setLastPage(limit);
        pageParam.setIsFirstPage(pageInfo.isIsFirstPage());
        //传递到 admin/userlist.html 的参数
        model.addAttribute("users",pageInfo.getList());
        model.addAttribute("page",pageParam);

        return "admin/userlist";

    }
}
