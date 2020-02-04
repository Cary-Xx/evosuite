package evosuite.shell.listmethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.CollectionUtil;
import org.evosuite.utils.CommonUtility;
import org.slf4j.Logger;

import evosuite.shell.EvosuiteForMethod;
import evosuite.shell.FileUtils;
import evosuite.shell.ParameterOptions;
import evosuite.shell.utils.LoggerUtils;
import evosuite.shell.utils.TargetMethodIOUtils;
import javassist.ClassPool;

/**
 * 
 * @author lyly
 * cmd: java -jar [EvosuiteTest.jar] -target !PROJECT!.jar -listMethods
 * return: a txt file which contain list of methods.[/evoTest-reports/targetMethods.txt]
 */
public class ListMethods {
	private static Logger log = LoggerUtils.getLogger(ListMethods.class);
	
	public static final String OPT_NAME = ParameterOptions.LIST_METHODS_OPT;

	public static int execute(String[] targetClasses, ClassLoader classLoader, MethodFilterOption mFilterOpt,
			String targetMethodFilePath, String targetClassFilePath)
			throws ClassNotFoundException, IOException {
		StringBuilder headerSb = new StringBuilder();
		headerSb.append("#------------------------------------------------------------------------\n")
			.append("#Project=").append(EvosuiteForMethod.projectName).append("  -   ").append(EvosuiteForMethod.projectId).append("\n")
			.append("#------------------------------------------------------------------------\n");
		log.info(headerSb.toString());
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", headerSb.toString(), true);
		FileUtils.writeFile(targetMethodFilePath, headerSb.toString(), true);
		if (!ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			Properties.CRITERION = ArrayUtils.addAll(Properties.CRITERION, Criterion.DEFUSE);
		}
		
		/**
		 * we clear the branch pool and graph pool when analyzing a new project.
		 */
		BranchPool.getInstance(classLoader).reset();
		GraphPool.getInstance(classLoader).clear();
		
		IMethodFilter methodFilter = mFilterOpt.getCorrespondingFilter();
		int total = 0;
		
        ((MethodFlagCondFilter) methodFilter).totalMethods = 0;
        ((MethodFlagCondFilter) methodFilter).ipfMethods = 0;
        ((MethodFlagCondFilter) methodFilter).noPrimitiveParameter = 0;
        ((MethodFlagCondFilter) methodFilter).interfaceOrAbstractParameter = 0;
        ((MethodFlagCondFilter) methodFilter).ipfNoPrimitiveParameter = 0;
        ((MethodFlagCondFilter) methodFilter).ipfInterfaceOrAbstractParameter = 0;
        ((MethodFlagCondFilter) methodFilter).ipfCannotInstrument = 0;
        
        ((MethodFlagCondFilter) methodFilter).targetMethods = 0;

		StringBuilder tMethodSb = new StringBuilder(headerSb.toString());
		List<String> testableClasses = new ArrayList<String>();
		for (String className : targetClasses) {
			try {
				Class<?> targetClass = classLoader.loadClass(className);
				// Filter out interface
				if (targetClass.isInterface()) {
					continue;
				}
				System.out.println("Class " + targetClass.getName());
				List<String> testableMethods = methodFilter.listTestableMethods(targetClass, classLoader);
				
				if (!CollectionUtil.isEmpty(testableMethods)) {
					testableClasses.add(className);
				}
				total += CollectionUtil.getSize(testableMethods);
				tMethodSb = new StringBuilder();
				for (String methodName : testableMethods) {
					tMethodSb.append(CommonUtility.getMethodId(className, methodName)).append("\n");
				}
				
				System.currentTimeMillis();
				/* log to targetMethod.txt file */
				FileUtils.writeFile(targetMethodFilePath, tMethodSb.toString(), true);
			} catch (Throwable t) {
				tMethodSb = new StringBuilder();
				tMethodSb.append("Error when executing class ").append(className);
				tMethodSb.append(t.getMessage());
				log.error("Error", t);
			}
		}
		
        int totalMethods = ((MethodFlagCondFilter) methodFilter).getTotal();
        int ipfMethods = ((MethodFlagCondFilter) methodFilter).getFiltered();
        int targetMethods = ((MethodFlagCondFilter) methodFilter).getTarget();
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "TotalMethods: " + totalMethods + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "IPFmethods : " + ipfMethods + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "targetNoPrimitiveParameter : " + ((MethodFlagCondFilter) methodFilter).noPrimitiveParameter + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "targetHasInterfaceOrAbstractParameter : " + ((MethodFlagCondFilter) methodFilter).interfaceOrAbstractParameter + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "ipfNoPrimitiveParameter : " + ((MethodFlagCondFilter) methodFilter).ipfNoPrimitiveParameter + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "ipfHasInterfaceOrAbstractParameter : " + ((MethodFlagCondFilter) methodFilter).ipfInterfaceOrAbstractParameter + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "ipfCannotInstrument : " + ((MethodFlagCondFilter) methodFilter).ipfCannotInstrument + "\n", true);
        FileUtils.writeFile("D:\\ziheng\\SF100-clean-windows\\stats.txt", "TargetMethods : " + targetMethods + "\n", true);
        

		/* log target classes */
		TargetMethodIOUtils.writeTargetClassOrMethodTxt(EvosuiteForMethod.projectName, EvosuiteForMethod.projectId, 
				testableClasses, targetClassFilePath);
		return total;
	}
	
}
