package com.shuwa.treefrog.web;

import com.shuwa.treefrog.constant.UserConstant;
import com.shuwa.treefrog.entity.User;
import com.shuwa.treefrog.model.PageParam;
import com.shuwa.treefrog.service.impl.UserService;
import com.shuwa.treefrog.util.sendSMS.SmsSDKDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    UserService userService; // userervice -> userService

    @Autowired
    private User user; //这里给 User.java 头部加了个 @Component 修饰符

    @Autowired
    private SmsSDKDemo smsSDKDemo; //控制短信服务的功能类

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 编辑个人信息
     * @return 用户修改页面
     */
    @GetMapping("/updatePage")
    public String toChangePage(){
        logger.info("UserController->toChangePage");
        return "user";
    }

    /**
     * 提交修改
     * @param user
     * @return
     */
    @PostMapping("/updateEmp")
    public String update(User user,HttpSession session,Model model){
        logger.info("UserController->update");
        User u = (User) session.getAttribute("user");
        user.setId(u.getId());
        if( userService.update(user)){
            session.setAttribute("user", userService.get(u.getId()));
            model.addAttribute("message","修改信息成功");
        }
        else {
            model.addAttribute("message","修改信息失败");
        }
        return "user";
    }

    /**
     * 登陆
     * @return
     */
    @GetMapping("/login")
    public String toLoginPage(){
        return "login";
    }

    /**
     * 注销
     * @param username
     * @param password
     * @param map
     * @param session
     * @return
     */
    @RequestMapping("/user_login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password
            , Map<String,Object> map, HttpSession session){

        User user = userService.login(username,password);
        if(user != null){
            session.setAttribute("loginUser",username);  //登录成功将用户名存入会话对象
            session.setAttribute("userId",user.getId());  //存入用户ID
            session.setAttribute("user",user);
            map.put("user",user);
            //第一次进入主页初始化分页
            map.put("page",new PageParam());
            return "index";
        }else {
            map.put("msg", "用户名或密码错误");
            return "login";
        }
    }


    /**
     * 注销登录
     * 重定向前台页面。清空session中的缓存
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpSession session){

        session.removeAttribute("loginUser");
        session.removeAttribute("userId");
        return "redirect:login"; //返回主页
    }


    /**
     * 用户注册功能
     * @param userName
     * @param password
     * @param phone
     * @param request
     * @return
     */
//    @AvoidDuplicateSubmission(needSaveToken = true) //避免重复提交表单的注解
    @PostMapping(value = "/signup")
    public String signup(@RequestParam("userName") String userName
            , @RequestParam("password") String password
            , @RequestParam("phone") String phone
            , HttpServletRequest request) {
        logger.info("UserController->signup");
        boolean isNotAllowRegister = userService.isUserNameExist(userName);
        if (isNotAllowRegister) {
            UserConstant.errorMap.put("userNameDup", "用户名已存在！");
            request.setAttribute("errorMap", UserConstant.errorMap);
            return "signup";
        } else {
            user.setUsername(userName);
            user.setPassword(password);
            user.setPhone(phone);
            userService.register(user);
            return "login";
        } //end if...else
    } //end signup()

    /**
     * 传递到 邮件发送器
     * 主页面
     *
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/sms")
    public String smsSender(HttpServletRequest request) {
        logger.info("UserController->smsSender");
        //得到json参数值（phone）
        String phoneNum = request.getParameter("phone");
        //需要对手机号进行验证
        if (!userService.isPhoneExist(phoneNum)) {
            UserConstant.errorMap.put("phoneDup", "手机号已注册！");
        }
//        int verifiCode = getVerifCode();
        int verifiCode = 111111;
        //调用SmsSDKDemo发送手机验证码
//        smsSDKDemo.sendSms(phoneNum,verifiCode);
        //将结果存入session中
        request.getSession().setAttribute("verifiCode", verifiCode + ""); // 将验证码保存在session中        //返回发送的结果（成功，失败）
        return "11111";
    }

    /**
     * 生成六位数验证码
     *
     * @return
     */
    protected int getVerifCode() {
        // 100000~999999
        int max = 999999;
        int min = 100000;
        int verifCode = min + (int) (Math.random() * (max - min + 1));
        return verifCode;
    }// end getVerifCode()

    /**
     * 编辑个人信息
     * [https://blog.csdn.net/leo3070/article/details/81046383]
     * @return 用户修改页面
     */
    @RequestMapping("/userUpdate/{id}")
    public String userUpdate(@PathVariable("id") Integer id
            , Model model
            , HttpSession session) {
        logger.info("UserController->userUpdate");
        User user = userService.get(id);
        session.setAttribute("userId", user.getId());
        model.addAttribute("user", user);
        userService.update(user);
        return "admin/userUpdate";
    }


}
