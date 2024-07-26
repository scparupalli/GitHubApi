package com.github.listners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class ReportGenerator implements ITestListener {
	
	private static final Logger log = LogManager.getLogger(ReportGenerator.class);
	public final static String PROJECT_HOME = System.getProperty("user.dir");
	private static String EXTENT_REPORT_FILE = PROJECT_HOME + File.separator + "NewExtentReports" + File.separator
			+ "CompleteReport" + new SimpleDateFormat("dd-MM-yyyy HH-mm").format(new Date()) + ".html";
	private static boolean didOnTestStartGotExecuted = false;
	public String filename;
	public ExtentTest logger;
	public ExtentTest moduleWiseLogger;
	Date startTime, endTime;
	private String EXTENT_REPORT_PATH = PROJECT_HOME + File.separator + "NewExtentReports" + File.separator
			+ "SuiteWiseReports" + File.separator;
	private ExtentSparkReporter htmlreporter = new ExtentSparkReporter(EXTENT_REPORT_FILE);
	private ExtentSparkReporter moduleWiseHTMLReporter;
	private ExtentReports extent = new ExtentReports();
	private ExtentReports moduleWiseExtent;
	private String suiteName;
	private String methodParameters = "";

	public static String imgToBase64String(final RenderedImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
			return os.toString(StandardCharsets.ISO_8859_1.name());
		} catch (final IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static BufferedImage base64StringToImg(final String base64String) {
		try {
			return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
		} catch (final IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		try {
			endTime = new Date();
			String testName = result.getName();
			log.error("\n\n " + testName + methodParameters + " test has failed \n\n");
			logger.log(Status.FAIL, "<b><i> Failure Message :: </b></i>" + result.getThrowable().getMessage());
			moduleWiseLogger.log(Status.FAIL,
					"<b><i> Failure Message :: </b></i>" + result.getThrowable().getMessage());
			logger.log(Status.FAIL, "<div><b><i> Stacktrace :: </b></i></div><div><textarea>"
					+ result.getThrowable().getStackTrace() + "</textarea></div>");
			moduleWiseLogger.log(Status.FAIL, "<div><b><i> Stacktrace :: </b></i></div><div><textarea>"
					+ result.getThrowable().getStackTrace() + "</textarea></div>");
			logger.log(Status.INFO, "Total time taken for test case to execute :: "
					+ ((endTime.getTime() - startTime.getTime()) / 1000) + " Seconds");
			moduleWiseLogger.log(Status.INFO, "Total time taken for test case to execute :: "
					+ ((endTime.getTime() - startTime.getTime()) / 1000) + " Seconds");

			extent.flush();
			moduleWiseExtent.flush();
		} catch (Exception e) {
			log.error("After test failure things are getting failed due to  :: " + e.getMessage());
		} finally {
			didOnTestStartGotExecuted = false;
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		NumberFormat formatter = new DecimalFormat("###.##");
		log.info("----------------------------------------------------------------------------------------\n");
		log.info("****************************************************************************************\n");
	}

	@Override
	public void onTestStart(ITestResult result) {
		didOnTestStartGotExecuted = true;
		String testName = result.getName();
		Object[] params = result.getParameters();
		methodParameters = "";
		if (params.length > 0) {
			methodParameters += " ( ";
			for (int i = 0; i < params.length; i++) {
				if (i == 0) {
					methodParameters += params[i].toString();
				} else {
					methodParameters += " , " + params[i].toString();
				}
			}
			methodParameters += " ) ";
			methodParameters = methodParameters.replace("*", "");
		}
		log.info("\n\n" + "<< --- TestCase START --->> " + testName + methodParameters + "\n");
		logger = extent.createTest(testName + methodParameters);
		logger.assignCategory(suiteName);
		logger.log(Status.INFO, "<b><i>Test Case Name :: </b></i>\"" + testName + "\"");
		logger.log(Status.INFO,
				"<b><i>Description of the test :: </b></i> \"" + result.getMethod().getDescription() + "\"");
		moduleWiseLogger = moduleWiseExtent.createTest(testName + methodParameters);
		moduleWiseLogger.assignCategory(suiteName);
		moduleWiseLogger.log(Status.INFO, "<b><i>Test Case Name :: </b></i>\"" + testName + "\"");
		moduleWiseLogger.log(Status.INFO,
				"<b><i>Description of the test :: </b></i> \"" + result.getMethod().getDescription() + "\"");
		startTime = new Date();
		filename = testName;
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		endTime = new Date();
		String testName = result.getName();
		log.info("\n\n TestCase: " + testName + methodParameters + ": --->>> PASS \n");
		logger.log(Status.INFO, "Total time taken for test case to execute :: "
				+ ((endTime.getTime() - startTime.getTime()) / 1000) + " Seconds");
		logger.log(Status.PASS, testName + methodParameters + " test has passed");
		moduleWiseLogger.log(Status.INFO, "Total time taken for test case to execute :: "
				+ ((endTime.getTime() - startTime.getTime()) / 1000) + " Seconds");
		moduleWiseLogger.log(Status.PASS, testName + methodParameters + " test has passed");
		extent.flush();
		moduleWiseExtent.flush();
		didOnTestStartGotExecuted = false;
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String testName = result.getName();
		if (!didOnTestStartGotExecuted) {
			logger = extent.createTest(testName + methodParameters);
			logger.assignCategory(suiteName);
			moduleWiseLogger = moduleWiseExtent.createTest(testName + methodParameters);
			moduleWiseLogger.assignCategory(suiteName);
		}
		logger.log(Status.INFO,
				"<b><i>Description of the test :: </b></i>\"" + result.getMethod().getDescription() + "\"");
		logger.log(Status.SKIP,
				testName + methodParameters + " test skipped due to :: " + result.getThrowable().getStackTrace());
		moduleWiseLogger.log(Status.INFO,
				"<b><i>Description of the test :: </b></i>\"" + result.getMethod().getDescription() + "\"");
		moduleWiseLogger.log(Status.SKIP, "<b><i>" + "Test skipped due to :: </b></i><textarea>"
				+ result.getThrowable().getStackTrace() + "</textarea>");
		log.info("\n\n TestCase: " + testName + methodParameters + ": --->>> SKIPPED");
		extent.flush();
		moduleWiseExtent.flush();
		didOnTestStartGotExecuted = false;
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		log.info("\n\n TestCase: " + result.getName() + ": --->>> FAILED With percentage");
	}

	@Override
	public void onStart(ITestContext context) {
		log.info("****************************************************************************************");
		log.info("                                " + context.getName() + "       ");
		log.info("----------------------------------------------------------------------------------------");
		File extentReports = new File(EXTENT_REPORT_PATH);
		if (!extentReports.exists()) {
			extentReports.mkdirs();
		}
		extent.attachReporter(htmlreporter);
		htmlreporter.config().setDocumentTitle("Automation Report");
		htmlreporter.config().setReportName("Automation Report");
		htmlreporter.config().setTheme(Theme.DARK);
		suiteName = context.getName();
		moduleWiseHTMLReporter = null;
		moduleWiseExtent = null;
		moduleWiseExtent = new ExtentReports();
		moduleWiseHTMLReporter = new ExtentSparkReporter(
				EXTENT_REPORT_PATH + File.separator + suiteName.replace(":", "") + " Report.html");
		moduleWiseExtent.attachReporter(moduleWiseHTMLReporter);
		moduleWiseHTMLReporter.config().setDocumentTitle(suiteName + " Automation Report");
		moduleWiseHTMLReporter.config().setReportName(suiteName + " Automation Report");
		moduleWiseHTMLReporter.config().setTheme(Theme.STANDARD);
	}
}
