package servlet.user;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import pojo.Role;
import pojo.User;
import service.role.RoleServiceImpl;
import service.user.UserService;
import service.user.UserServiceImpl;
import util.Constants;
import util.PageSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

//实现servlet复用
public class UserServlet extends HttpServlet {
    //添加一个if判断
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)throws ServletException,IOException {
        String method = req.getParameter("method");
        if (method.equals("savepwd")&&method!=null){
            this.updatePwd(req,resp);
        }else if (method.equals("pwdmodify")&&method!=null){
            this.pwdModify(req, resp);
        }else if (method.equals("query")&&method!=null){
            this.query(req, resp);

        }

    }

    private void pwdModify(HttpServletRequest req, HttpServletResponse resp) {

    }

    private void updatePwd(HttpServletRequest req, HttpServletResponse resp) {
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }


    //修改密码
    public void modifyPwd(HttpServletRequest req, HttpServletResponse resp) {
        //获取要修改的密码和id
        Object attribute = req.getSession().getAttribute(Constants.USER_SESSION);
        String newpassword = req.getParameter("newpassword");
        boolean flag = false;
        //判断这个session和新密码是否存在
        if (attribute != null && !StringUtils.isNullOrEmpty(newpassword)) {
            UserServiceImpl userService = new UserServiceImpl();
            flag = userService.modifyPwd(((User) attribute).getId(), newpassword);
            if (flag) {
                req.setAttribute("message", "修改密码成功");
                //密码修改成功移除当前session
                req.getSession().removeAttribute(Constants.USER_SESSION);
            } else {
                req.setAttribute("message", "密码修改失败");
            }
        } else {
            //新密码有问题
            req.setAttribute("message", "新密码有问题");
        }
        try {
            req.getRequestDispatcher("pwdmodify.jsp").forward(req, resp);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //与旧密码进行比对
    public void pwdmodify(HttpServletRequest req, HttpServletResponse resp) {
        //旧密码就存放在session中直接在session中获取
        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        //与其做对比的密码
        String oldpassword = req.getParameter("oldpassword");
        System.out.println(oldpassword);
        //万能的Hashmap 一切的东西都可以存放进去
        HashMap<String, String> resultMap = new HashMap<String, String>();//resultMap结果集
        if (o == null) {//session失效了
            resultMap.put("result", "sessionerror");//给结果集传ajax设置的result
        } else if (oldpassword == null) {
            resultMap.put("result", "error");//给结果集传ajax设置的result
        } else {
            //密码不为空且session也没有失效 我们就可以通过session进行比对了！
            //同时因为设置的session为object的类型先要转换为User类型
            String userPassword = ((User) o).getUserPassword();//session中用户的密码
            if (oldpassword.equals(userPassword)) {
                System.out.println(oldpassword);
                resultMap.put("result", "ture");
            } else if (!oldpassword.equals(userPassword)) {
                resultMap.put("result", "false");
            }
        }

        try {//将数据以json的类型输出出去
            resp.setContentType("application/json");//就像resp.setContentType("text/html")
            //也是以流的形式去返回 注意关闭流的资源
            PrintWriter writer = resp.getWriter();
            //JSONArray是一个工具类，用于将数据类型转换为json类型
            /*
             *resultMap输出出来是{"result","sessionerror" , "result","error"}一对对的键值对				 HashMap<K,V>
             *     Json格式={key:value}
             * */
            writer.write(JSONArray.toJSONString(resultMap));
            //最后刷新和关闭流资源
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //query方法
    public void query(HttpServletRequest req, HttpServletResponse resp){

        //查询用户列表

        //从前端获取数据；
        String queryUserName = req.getParameter("queryname");
        String temp = req.getParameter("queryUserRole");
        String pageIndex = req.getParameter("pageIndex");
        int queryUserRole = 0;

        //获取用户列表
        UserServiceImpl userService = new UserServiceImpl();
        List<User> userList = null;

        //第一次走这个请求，一定是第一页，页面大小固定的；
        int pageSize = 5; //可以把这个些到配置文件中，方便后期修改；
        int currentPageNo = 1;

        if (queryUserName ==null){
            queryUserName = "";
        }
        if (temp!=null && !temp.equals("")){
            queryUserRole = Integer.parseInt(temp);  //给查询赋值！0,1,2,3
        }
        if (pageIndex!=null){
            currentPageNo = Integer.parseInt(pageIndex);
        }

        //获取用户的总数 (分页：  上一页，下一页的情况)
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);
        //总页数支持
        PageSupport pageSupport = new PageSupport();
        pageSupport.setCurrentPageNo(currentPageNo);
        pageSupport.setPageSize(pageSize);
        pageSupport.setTotalCount(totalCount);

        int totalPageCount = ((int)(totalCount/pageSize))+1;

        //控制首页和尾页
        //如果页面要小于1了，就显示第一页的东西
        if (currentPageNo<1){
            currentPageNo = 1;
        }else if (currentPageNo>totalPageCount){ //当前页面大于了最后一页；
            currentPageNo = totalPageCount;
        }

        //获取用户列表展示
        userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
        req.setAttribute("userList",userList);

        RoleServiceImpl roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList();
        req.setAttribute("roleList",roleList);
        req.setAttribute("totalCount",totalCount);
        req.setAttribute("currentPageNo",currentPageNo);
        req.setAttribute("totalPageCount",totalPageCount);
        req.setAttribute("queryUserName",queryUserName);
        req.setAttribute("queryUserRole",queryUserRole);


        //返回前端
        try {
            req.getRequestDispatcher("userlist.jsp").forward(req,resp);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //整体的Servlet
    public void init() throws ServletException {
        // Put your code here
    }
    public UserServlet() {
        super();
    }
    public void destroy() {
        super.destroy();
    }

    }


