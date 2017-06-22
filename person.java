package ccim.iar.ui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import ccim.iar.ui.main.userinput;

public class Person extends ScreenBase {

	final private static String RESTRICTED_ALERT_MESSAGE = "Restricted documents exist";

	private static final Logger log = LogManager.getLogger(Person.class);
	public SearchResults personList = null;
	public SearchResults assessmentList = null;
	public boolean restrictedAssessmentsExist = false; 
	public ArrayList<String> IDs;

	public int search(WebDriver driver, SearchCriteria searchcriteria) {

		setErrorMessage(null);
		setSuccess(true);
		setAction("Person Search");

		int rows = 0;
		String action = getAction();

		util.logActionStart(driver, action);

		util.waitThnkTime(util.thinkTime);

		try {
			if (moveToSearchCriteriaFrame(driver)) {

				// set-up search criteria
				String hcnUseField = searchcriteria.getField(SearchCriteria.USE_HCN);

				boolean useHCN = util.getBoolean(hcnUseField, false);
				String field = null;
				if (useHCN) {
					if ((field = searchcriteria.getField(SearchCriteria.HCN)) != null)
						driver.findElement(By.id("Ontario Health Card Number")).sendKeys(field);
				} else {
					if ((field = searchcriteria.getField(SearchCriteria.LAST_NAME)) != null)
						driver.findElement(By.id("Last Name")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.FIRST_NAME)) != null)
						driver.findElement(By.id("First Name")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.DOB)) != null)
						driver.findElement(By.id("Date of Birth")).sendKeys(field);
					else
						driver.findElement(By.id("Date of Birth")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.SEX)) != null)
						driver.findElement(By.id("Sex")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.STREET)) != null)
						driver.findElement(By.id("Street")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.UNIT)) != null)
						driver.findElement(By.id("Unit/Suite/Apt.")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.POSTAL_CODE)) != null)
						driver.findElement(By.id("Postal/Zip Code")).sendKeys(field);
					if ((field = searchcriteria.getField(SearchCriteria.PHONE)) != null)
						driver.findElement(By.id("Phone")).sendKeys(field);

				}
				driver.findElement(By.id("default-button-button")).click();

				long startTime = System.currentTimeMillis();

				if (moveToPersonSearchResultsFrame(driver)) {
					// Wait for the page to load, timeout after 10 seconds
					(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
						public Boolean apply(WebDriver d) {
							return d.findElement(By.id("SearchResults")).findElement(By.id("SearchResults")).isDisplayed();						}
					});
				} else {
					log.error("moveToSearchResultsFrame failed");
					return rows;
				}
				log.info(action + " duration (sec): " + (System.currentTimeMillis() - startTime) / 1000);

				savePersonSearchResults(driver);

				rows = personList.resultRows();

				/*
				 * WebElement searchResultsBody =
				 * driver.findElement(By.id("SearchResults")).findElement(By.id(
				 * "SearchResults")); if (!searchResultsBody.getText().contains(
				 * "No result found")) { List<WebElement> searchRows =
				 * searchResultsBody.findElements(By.tagName("tr")); rows =
				 * searchRows.size() - 1; }
				 */
				log.info(action + " done, found persons " + rows);
			} else {
				setErrorMessage(action + " - " + "moveToSearchCriteriaFrame failed");
				setSuccess(false);
				log.error("moveToSearchCriteriaFrame failed");
			}
		} catch (Exception e) {
			setErrorMessage(action + " - " + e.getLocalizedMessage());
			setSuccess(false);
			e.printStackTrace();
		}
		return rows;
	}

	/**
	 * 
	 * @param driver
	 * @param index,
	 *            starts with 0
	 * @return
	 */
	public boolean getDetails(WebDriver driver, int index) {

		setErrorMessage(null);
		setSuccess(true);
		setAction("Person getDetails");
		restrictedAssessmentsExist = false;

		String action = getAction();
		util.logActionStart(driver, action);
		try {
			util.waitThnkTime(util.thinkTime);

			if (isSearchResultsRowLocked(index)) {
				log.error("Search results row is locked: " + index);
				log.error(this.personList.getResultRow(index));
				return false;
			}

			if (moveToAssessmentListFrame(driver)) {

				driver.findElement(By.id("search-results-body"));
				// log.debug("Person Search Results " +
				// searchResultsBody.getText()); // SearchFilter
				WebElement searchResultsRow = driver.findElement(By.id("row" + index));
				searchResultsRow.click();

				long startTime = System.currentTimeMillis();

				// accept Restricted documents exist Alert if present
				String alertMessage = util.handleAlert(driver, true, 0);
				if (alertMessage != null && alertMessage.equals(Person.RESTRICTED_ALERT_MESSAGE)) {
					restrictedAssessmentsExist = true;
				}
				

				// Wait for the page to load, timeout after 10 seconds

				(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return !d.getTitle().contains("User");
					}
				});
				log.info(action + " duration (sec): " + (System.currentTimeMillis() - startTime) / 1000);

				// assessmentList = util.listAssessments(driver);
				saveAssessmentList(driver);
				

				log.info(action + " done, now on page " + driver.getTitle());
			 } else {
				 setErrorMessage(action + " - " + "moveToSearchCriteriaFrame failed");
				 setSuccess(false);
				 log.error("moveToSearchResultsFrame failed");
			 }
		} catch (Exception e) {
			setErrorMessage(action + " - " + e.getLocalizedMessage());
			setSuccess(false);
			e.printStackTrace();
		}

		return isSuccess();

	}

	

	/*
	 * private WebDriver moveToSearchFrame(WebDriver driver) {
	 * driver.switchTo().defaultContent(); // you are now outside frames
	 * driver.switchTo().frame("ConcertoContext");
	 * driver.switchTo().frame("application-content");
	 * driver.switchTo().frame(0); // frame("Person Search"); return driver; }
	 */
	public boolean moveToSearchCriteriaFrame(WebDriver driver) {
		String pmapfilename = "Iframe__2__2__1__0__.html";
		log.debug("... getting to : " + pmapfilename);
		boolean r = false;
		if (PageMap.gotoFrame(driver, pmapfilename, "0")) {
			// log.debug("SearchResultsFrame source: "+driver.getPageSource());
			r = true;
		} else {
			r = false;
			log.error("Cannot get search criteria");
		}
		return r;
	}

	public boolean moveToPersonSearchResultsFrame(WebDriver driver) {
		String pmapfilename = "Iframe__2__2__1__0__.html";
		//PageMap pagemap = new PageMap(true, true, true, userinput.pagesource_folder);
		//pagemap.getPageMap(driver);
		log.debug("... getting to : " + pmapfilename);
		boolean r = false;
		if (PageMap.gotoFrame(driver, pmapfilename, "1")) {
			r = true;
		} else {
			r = false;
			log.error("Cannot get search results");
		}
		return r;
	}

	public boolean moveToAssessmentListFrame(WebDriver driver) {
		String pmapfilename = "Iframe__3__2__2__2__2__.html";
		log.debug("... getting to : " + pmapfilename);
		boolean r = false;
		if (PageMap.gotoFrame(driver, pmapfilename, null)) {
			r = true;
		} else {
			r = false;
			log.error("Cannot get search results");
		}
		return r;
	}

	public boolean isSearchResultsRowLocked(int rowNumber) {
		boolean r = false;
		if (this.personList.getResultRow(rowNumber).get(0).equals("Locked"))
			r = true;
		return r;
	}

	private void savePersonSearchResults(WebDriver driver) {

		// search results frame
		if (moveToPersonSearchResultsFrame(driver)) {
			this.personList = new SearchResults();
			List<WebElement> header = driver.findElements(By.xpath("//div/table/thead/tr/th/div/span/a"));
			ArrayList<String> headerRow = new ArrayList<String>(); // List<WebElement>
																	// header =
																	// driver.findElement(By.tagName("thead")).findElements(By.tagName("a"));
			headerRow.add("Locked");
			for (WebElement colName : header) { 
				headerRow.add(colName.getAttribute("innerHTML").substring(0,
						colName.getAttribute("innerHTML").indexOf("<span")));
			}
			personList.addHeader(headerRow);
			int columns = headerRow.size();
			List<WebElement> data = driver.findElements(By.xpath("//div/table/tbody/tr/td/div"));
			// List<WebElement> header =
			// driver.findElement(By.tagName("thead")).findElements(By.tagName("a"));
			int columnInt = columns + 1;
			ArrayList<String> resultsRow = null;
			for (WebElement colValue : data) {
				if (columnInt >= columns) {
					columnInt = 0;
					resultsRow = new ArrayList<String>();
					personList.addResultRow(resultsRow);
				} else
					columnInt++;
				if (columnInt > 0) { // skip first column
					if (columnInt == 1) { // locked column
						resultsRow.add(colValue.getAttribute("class").trim());
					} else
						resultsRow.add(colValue.getAttribute("innerHTML").trim());
				}
			}

			log.info("searchResults: " + personList.getResults().toString());
		} else {
			log.error("Cannot get search results");
		}
	}

	private String saveAssessmentList(WebDriver driver) {

		// search results frame
		if (moveToAssessmentListFrame(driver)) {
			this.assessmentList = new SearchResults();
			List<WebElement> header = driver.findElements(By.xpath("//div/table/thead/tr/th/div/span/a"));
			ArrayList<String> headerRow = new ArrayList<String>(); // List<WebElement>
																	// header =
																	// driver.findElement(By.tagName("thead")).findElements(By.tagName("a"));
			for (WebElement colName : header) {
				// headerRow.add(colName.getText().trim());
				String inner = colName.getAttribute("innerHTML");
				int idx = inner.indexOf("<span");
				headerRow.add(inner.substring(0, idx == -1 ? 0 : idx));
			}

			assessmentList.addHeader(headerRow);
			int columns = headerRow.size();

			List<WebElement> data = driver.findElements(By.xpath("//div/table/tbody/tr/td/div"));
			int columnInt = columns + 1;
			ArrayList<String> resultsRow = null;
			for (WebElement colValue : data) {
				if (columnInt >= columns) {
					columnInt = 0;
					resultsRow = new ArrayList<String>();
					assessmentList.addResultRow(resultsRow);
				}
				log.info("1 assessmentList: " + assessmentList.getResults().toString());
				columnInt++;
				resultsRow.add(colValue.getText().trim());
			}

			List<WebElement> lines = driver.findElements(By.xpath("//div/table/tbody/tr"));
			for (WebElement line : lines) {
				util.waitSeconds(driver, 2);
				line.click();
				String message = util.handleAlert(driver, true, 2);
				System.out.println("mmmmmmm: "+ message);
				Map<String, String> lineDetails = util.parsePopupMessage(message, 9, "\n", ":\t");
				String id = lineDetails.get("Assessment ID");
				log.info("captured the assessment id: " +id);
				if(userinput.upload_assessment) {
				if (CheckIfIdMatch(id)) 
						log.info("Can comfirm the assessment IDs: " + id +" are the same");
					else 
						{log.error("This assessment is not the one uploaded! This one is "+ id);
						System.out.println("FAILED");
						System.exit(0);} 
				}
				//System.out.println("map: "+ lineDetails);
				assessmentList.addResultDetailsRow(lineDetails);
				
			}

			log.info("assessmentList result detail: " + assessmentList.getResultsDetailRow().toString());
			 
		} else {
			log.error("Cannot get search results");
		}
		
		
		
		return null;
	}

 
	private boolean CheckIfIdMatch(String id) {
		boolean boo = report.assessmentidlist.contains(id);
		if (boo)
			report.assessmentidlist.remove(id);
		util.waitThnkTime(4);
		return boo;
		
	}
}
