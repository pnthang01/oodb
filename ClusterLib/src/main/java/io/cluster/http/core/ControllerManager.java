/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.core;

import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestParam;
import io.cluster.util.ClassLoaderUtil;
import io.cluster.util.MethodUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ControllerManager {

    private static final Logger LOGGER = LogManager.getLogger(ControllerManager.class.getName());

    private Map<String, MethodMapping> controllerMap;

    private static ControllerManager _instance;

    public ControllerManager(List<Class<?>> classes) {
        controllerMap = new ConcurrentHashMap();
        LOGGER.info("Initialize total " + classes.size() + " controllers. Start to mapping...");
        for (Class clazz : classes) {
            try {
                RequestMapping anno = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                if (null != anno) {
                    String moduleMapping = anno.uri();
                    AbstractController newInstance = (AbstractController) clazz.newInstance();
                    for (Method method : clazz.getDeclaredMethods()) {
                        RequestMapping methodAnno = method.getAnnotation(RequestMapping.class);
                        if (null != methodAnno) {
                            String finalMapping = moduleMapping + methodAnno.uri();
                            LOGGER.info("Follwing uri is mapped: " + finalMapping + " method=" + method.getName()
                                    + " parameters.size=" + method.getParameterTypes().length);
                            MethodMapping methodMapping = new MethodMapping(newInstance, method);
                            controllerMap.put(finalMapping, methodMapping);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.error("Cannot auto mapping to class: " + clazz.getSimpleName() + " with error: %s", ex);
            }
        }
    }

    public static Object invokeUri(String uri, Map<String, List<String>> params) {
        try {
            MethodMapping get = _instance.controllerMap.get(uri);
            if (null == get) {
                LOGGER.error("Uri " + uri + " is 404, cannot invoke.");
                return 404;
            } else {
                Method method = get.getMethod();
                LOGGER.info("Method name: " + method.getName());
                Class<?>[] paramClasses = method.getParameterTypes();
//            Class<?>[] parametersType = method.getParameterTypes();
//            Annotation[][] paramAnno = method.getParameterAnnotations();
                Object[] paramValue = new Object[paramClasses.length];
                for (int i = 0; i < paramClasses.length; ++i) {
//                Parameter param = parameters[i];
//                Class<?> type = parametersType[i];
                    Class<?> type = paramClasses[i];
                    RequestParam annotation = (RequestParam) method.getParameterAnnotations()[i][0];
//                RequestParam annotation = param.getAnnotation(RequestParam.class);
                    List<String> paramList = params.get(annotation.name());
                    String value = paramList == null ? null : paramList.get(0);
                    if (null == value || value.isEmpty()) {
                        if (annotation.required()) {
                            LOGGER.error("Param is missing with index: " + i);
                            return 504;
                        }
                        paramValue[i] = (annotation.defaultValue() != null && !annotation.defaultValue().isEmpty())
                                ? castParamValue(type, annotation.defaultValue()) : null;
                    } else {
                        paramValue[i] = castParamValue(type, value);
                    }
                }
                return method.invoke(get.getController(), paramValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error when invokeUri: %s", ex);
            return null;
        }
    }

    private static Object castParamValue(Class<?> type, String value) {
        if (java.lang.Integer.TYPE.equals(type) || int.class.equals(type)) {
            return Integer.valueOf(value);
        } else if (java.lang.Long.TYPE.equals(type) || long.class.equals(type)) {
            return Long.valueOf(value);
        } else if (java.lang.Double.TYPE.equals(type) || double.class.equals(type)) {
            return Double.valueOf(value);
        } else if (java.lang.Float.TYPE.equals(type) || float.class.equals(type)) {
            return Float.valueOf(value);
        } else if (java.lang.Boolean.TYPE.equals(type) || boolean.class.equals(type)) {
            return Boolean.valueOf(value);
        } else if (java.lang.String.class.equals(type)) {
            return value;
        } else {
            return MethodUtil.fromJson(value, type);
        }
    }

    private class MethodMapping {

        private final AbstractController controller;
        private final Method method;

        public MethodMapping(AbstractController controller, Method method) {
            this.controller = controller;
            this.method = method;
        }

        public AbstractController getController() {
            return controller;
        }

        public Method getMethod() {
            return method;
        }
    }

    public static void initialize() throws ClassNotFoundException, IOException {
//        Class[] classes = getClasses("com.ants.apiservice.http.controller");
        List<Class<?>> find = ClassLoaderUtil.getClassesForPackage("io.cluster.http.controller");
        _instance = new ControllerManager(find);
    }

}
