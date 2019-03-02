package com.test;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.junit.Test;

import evosuite.shell.EvoTestResult;

public class MathUtilTest extends AbstractETest{
	
	@Test
	public void test() {
		Class<?> clazz = org.apache.commons.math.util.MathUtils.class;
		String methodName = "equals";
		int parameterNum = 3;
		
		String targetClass = clazz.getCanonicalName();
//		Method method = clazz.getMethods()[0];
//		Method method = getTragetMethod(methodName, clazz, parameterNum);

		String targetMethod = "equals(FFF)Z";
		String cp = "target/classes;lib/commons-math-2.2.jar";

		// Properties.LOCAL_SEARCH_RATE = 1;
//		Properties.DEBUG = true;
//		Properties.PORT = 8000;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;
		
//		Properties.LOCAL_SEARCH_BUDGET = 1000;
		Properties.SEARCH_BUDGET = 60000;
		Properties.GLOBAL_TIMEOUT = 60000;
		Properties.TIMEOUT = 30000;
//		Properties.TIMELINE_INTERVAL = 3000;
		int timeBudget = 600000;
		
		int repeat = 1;
		List<EvoTestResult> l1 = new ArrayList<>();
		List<EvoTestResult> l2 = new ArrayList<>();
		
		
		String fitnessApproach = "fbranch";
		l1 = runRepetativeTimes(targetClass, targetMethod, cp, timeBudget, fitnessApproach, repeat);
		
//		fitnessApproach = "branch";
//		l2 = runRepetativeTimes(targetClass, targetMethod, cp, timeBudget, fitnessApproach, repeat);
		
		System.out.println("fbranch" + ":");
		for(EvoTestResult lu: l1){
			System.out.println(lu.getCoverage());
			System.out.println(lu.getProgress());
		}
		
		System.out.println("branch" + ":");
		for(EvoTestResult lu: l2){
			System.out.println(lu.getCoverage());
			System.out.println(lu.getProgress());
		}
	}

	private List<EvoTestResult> runRepetativeTimes(String targetClass, String targetMethod, String cp, int timeBudget, String fitnessApproach, int repeat) {
		MathUtilTest t = new MathUtilTest();
		List<EvoTestResult> l = new ArrayList<>();
		for(int i=0; i<repeat; i++){
			try {
				EvoTestResult tu = t.evosuite(targetClass, targetMethod, cp, timeBudget, true, fitnessApproach);
				l.add(tu);		
//				Thread.sleep(60000);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return l;
	}

}