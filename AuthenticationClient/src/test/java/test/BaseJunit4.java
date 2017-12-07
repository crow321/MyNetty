package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import static org.junit.Assert.*;

/**
 * @author Created by zhangp on 2017/9/27.
 * @version v1.0.0
 */
public class BaseJunit4 {
    @Rule
    public TestName testName = new TestName();
    private long startTime;
    private long finishTime;

    @Before
    public void setUp() throws Exception {
        System.out.println("测试方法名称: " + testName.getMethodName());
        startTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() throws Exception {
        finishTime = System.currentTimeMillis();
        System.out.println(testName.getMethodName() + "耗时:" + (finishTime - startTime));
        System.out.println("---------------------------------------------------------------------------------------------\n");
    }
}
