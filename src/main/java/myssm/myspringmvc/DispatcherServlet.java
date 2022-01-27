package myssm.myspringmvc;

import myssm.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@WebServlet("*.do")
public class DispatcherServlet extends HttpServlet {

    private Map<String,Object> beanMap = new HashMap<>();

    public DispatcherServlet(){
    }

    @Override
    public void init(){
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("applicationContext.xml");
            //1.创建DocumentBuilderFactory
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //2.创建DocumentBuilder对象
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder() ;
            //3.创建Document对象
            Document document = documentBuilder.parse(inputStream);

            //4.获取所有的bean节点
            NodeList beanNodeList = document.getElementsByTagName("bean");
            for(int i = 0 ; i<beanNodeList.getLength() ; i++){
                Node beanNode = beanNodeList.item(i);
                if(beanNode.getNodeType() == Node.ELEMENT_NODE){
                    Element beanElement = (Element)beanNode ;
                    String beanId =  beanElement.getAttribute("id");
                    String className = beanElement.getAttribute("class");
                    Class controllerBeanClass = Class.forName(className);
                    Object beanObj = controllerBeanClass.newInstance() ;
                    // 创建 setServletContext 是为了在 ViewBaseServlet 中传入 servletContext
                    Method setServletContextMethod = controllerBeanClass.getDeclaredMethod("setServletContext",ServletContext.class);
                    setServletContextMethod.invoke(beanObj , this.getServletContext());

                    beanMap.put(beanId , beanObj) ;
                }
            }
        } catch (ParserConfigurationException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //设置编码
        request.setCharacterEncoding("UTF-8");
        //假设url是：  http://localhost:8080/pro15/hello.do
        //那么servletPath是：    /hello.do
        // 我的思路是：
        // 第1步： /hello.do ->   hello   或者  /fruit.do  -> fruit
        String servletPath = request.getServletPath();
        servletPath = servletPath.substring(1);
        int lastDotIndex = servletPath.lastIndexOf(".do") ;
        servletPath = servletPath.substring(0,lastDotIndex);

        // 第2步： hello -> HelloController 或者 fruit -> FruitController
        // servletPath = fruit
        Object controllerBeanObj = beanMap.get(servletPath);

        // 获取请求的方法
        String operate = request.getParameter("operate");
        if(StringUtil.isEmpty(operate)){
            operate = "index" ;
        }

        try {
            // 原来是获取提供的所有方法,与请求的做匹配
            // 现在直接去找那个operate要的方法
            //  public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
            // 直接得到 private void edit(HttpServletRequest request , HttpServletResponse response)之类的......
            Method method = controllerBeanObj.getClass().getDeclaredMethod(operate,HttpServletRequest.class,HttpServletResponse.class);
            if(method!=null){
                method.setAccessible(true);
                method.invoke(controllerBeanObj,request,response);
            }else{
                throw new RuntimeException("operate值非法!");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
