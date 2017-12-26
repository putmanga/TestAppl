package com.company;

import com.company.Annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_GRAY= "\u001B[37m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static Map<TestStatus, List<Method>> stats = new HashMap<>();

    private static TestClass instance;

    public static void main(String[] args) throws
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {

        instance = TestClass.class.newInstance();

        setup();
        runTests();
        destroy();

        printStatistics();
    }

    private static void setup() throws InvocationTargetException, IllegalAccessException {
        long count = countMethodsWithAnnotation(Setup.class);

        if (count == 0) {
            return;
        }
        if (count > 1) {
            throw new IllegalArgumentException("Wrong amount of setup methods: " + count);
        }
        Method method = Arrays.stream(TestClass.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Setup.class))
                .findFirst().get();

        method.invoke(instance);
    }

    private static long countMethodsWithAnnotation(Class<? extends Annotation> annotationClass) {
        return Arrays.stream(TestClass.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotationClass))
                .count();
    }

    private static void runTests() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        for (Method m : TestClass.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                if (m.getAnnotation(Test.class).isEnabled()) {
                    runBefore();
                    try {
                        m.invoke(instance);
                        printMessage(TestStatus.PASSED, m.getName() + " passed");
                        addToMap(TestStatus.PASSED, m);
                    } catch (InvocationTargetException e) {
                        if (m.isAnnotationPresent(Expected.class) &&
                                //instanceOf and .isInstance are not working for me here
                                // or I used them not correctly
                                m.getAnnotation(Expected.class).expected().equals(e.getCause().getClass())) {
                            printMessage(TestStatus.PASSED, m.getName() + " passed");
                            addToMap(TestStatus.PASSED, m);
                        } else {
                            printMessage(TestStatus.FAILED, m.getName() + " failed on " + e.getCause().getClass());
                            addToMap(TestStatus.FAILED, m);
                        }
                    } finally {
                        runAfter();
                    }
                } else {
                    addToMap(TestStatus.SKIPPED, m);
                }
            }
        }

    }

    private static void printMessage(TestStatus status, String message) {
        System.out.println(getConsoleColor(status) + message + ANSI_RESET);
    }

    private static void runBefore() throws InvocationTargetException, IllegalAccessException {
        List<Method> methods = Arrays.stream(TestClass.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Before.class))
                .collect(Collectors.toList());

        for (Method method : methods) {
            method.invoke(instance);
        }
    }

    private static void runAfter() throws InvocationTargetException, IllegalAccessException {
        List<Method> methods = Arrays.stream(TestClass.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(After.class))
                .collect(Collectors.toList());

        for (Method method : methods) {
            method.invoke(instance);
        }
    }

    private static void destroy() throws InvocationTargetException, IllegalAccessException {
        long count = countMethodsWithAnnotation(Destroy.class);

        if (count == 0) {
            return;
        }
        if (count > 1) {
            throw new IllegalArgumentException("Wrong amount of destroy methods: " + count);
        }
        Method method = Arrays.stream(TestClass.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Destroy.class))
                .findFirst().get();

        method.invoke(instance);
    }

    private static void addToMap(TestStatus status, Method m) {
        stats.putIfAbsent(status, new ArrayList<Method>());
        stats.get(status).add(m);
    }

    private static void printStatistics() {
        System.out.println("--------------------");
        System.out.println("Statistics:");
        for (Map.Entry<TestStatus, List<Method>> pair : stats.entrySet()) {
            System.out.println(String.format("%s%s : %s%s",
                    getConsoleColor(pair.getKey()),
                    pair.getKey(),
                    pair.getValue().size(),
                    ANSI_RESET));
        }
        System.out.println("--------------------");
    }

    private static String getConsoleColor(TestStatus key) {
        String ret;
        switch (key) {
            case PASSED:
                ret = ANSI_GREEN;
                break;
            case FAILED:
                ret = ANSI_RED;
                break;
            case SKIPPED:
                ret = ANSI_GRAY;
                break;
            default:
                ret = "";
        }

        return ret;
    }
}