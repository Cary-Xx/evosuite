package evosuite.shell;

import org.evosuite.utils.ProgramArgumentUtils;

public class Settings {
	public static final int DEFAULT_ITERATION = 1;
	public static final String DEFAULT_REPORT_FOLDER_NAME = "evoTest-reports";

	private static boolean listMethods;
	private static String inclusiveFilePath;
	private static int iteration = DEFAULT_ITERATION;
	private static String reportFolder = DEFAULT_REPORT_FOLDER_NAME;

	public static void setup(String[] args) throws Exception {
		listMethods = ProgramArgumentUtils.hasOpt(args, ListMethods.OPT_NAME);
		String optValue = ProgramArgumentUtils.getOptValue(args, ParameterOptions.METHOD_TEST_ITERATION);
		if (optValue != null) {
			iteration = Integer.valueOf(optValue);
		}
		optValue = ProgramArgumentUtils.getOptValue(args, ParameterOptions.REPORT_FOLDER);
		if (optValue != null) {
			reportFolder = optValue;
		} else {
			optValue = ProgramArgumentUtils.getOptValue(args, "-criterion");
			if (optValue == null) {
				optValue = ProgramArgumentUtils.getOptValue(args, "-Dcriterion");
			}
			if (optValue != null) {
				StringBuilder sb = new StringBuilder().append("report")
						.append("_").append(optValue)
//						.append("_").append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
//						.append(Month.of(Calendar.getInstance().get(Calendar.MONTH)))
//						.append("-").append(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)).append("h")
//						.append(Calendar.getInstance().get(Calendar.MINUTE))
						;		
				reportFolder = sb.toString();
			}
		}
	}

	public static boolean isListMethods() {
		return listMethods;
	}

	public static String getInclusiveFilePath() {
		return inclusiveFilePath;
	}

	public static int getIteration() {
		return iteration;
	}

	public static String getReportFolder() {
		return reportFolder;
	}

}