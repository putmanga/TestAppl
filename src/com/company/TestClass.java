package com.company;

import com.company.Annotations.*;

public class TestClass {
    private int setupCounter = 0;
    private int beforeCounter = 0;

    @Setup
    public void setup() {
        setupCounter++;
    }

    @Destroy
    public void destroy() {
        System.out.println("setupCounter: " + setupCounter);
        System.out.println("beforeCounter: " + beforeCounter);
    }

    @Before
    public void before1() {
        beforeCounter++;
    }

    public void test() {
        System.out.println("test");
    }

    @Test
    public void annotatedTest() {
        System.out.println("annotatedTest");
    }

    @Test
    public void testTrue() {
    }

    @Test
    public void testException() throws Exception {
        throw new Exception();
    }

    @Expected(expected = NullPointerException.class)
    @Test
    public void testNullPointerException() throws Exception {
        throw new NullPointerException();
    }

    @Test(isEnabled = false)
    public void annotatedTestFalse() {
        System.out.println("annotatedTestFalse");
    }
}
