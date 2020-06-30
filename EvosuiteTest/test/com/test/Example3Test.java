package com.test;

import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.utils.MethodUtil;
import org.junit.Test;

import com.example.Example3;

public class Example3Test extends TestUtility{
	
	@Test
	public void test() {
		Class<?> clazz = Example3.class;
		String methodName = "test";
		int parameterNum = 10;
		
		String targetClass = clazz.getCanonicalName();
//		Method method = clazz.getMethods()[0];
		Method method = TestUtility.getTargetMethod(methodName, clazz, parameterNum);

		String targetMethod = method.getName() + MethodUtil.getSignature(method);
		String cp = "target/classes";

		// Properties.LOCAL_SEARCH_RATE = 1;
//		Properties.DEBUG = true;
//		Properties.PORT = 8000;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;

		Properties.TIMEOUT = 100000;
//		Properties.TIMELINE_INTERVAL = 3000;
		
		String fitnessApproach = "fbranch";
		
		Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = 30000;
		
		int timeBudget = 30000;
		TestUtility.evosuite(targetClass, targetMethod, cp, timeBudget, true, fitnessApproach);
	}

	

}
