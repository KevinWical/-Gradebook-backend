package com.cst438;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

@SpringBootTest
public class EndToEndCreateAssignment
{
   public static final String CHROME_DRIVER_FILE_LOCATION = 
         "C:/Users/KevinAdmin/Downloads/chromedriver_win32/chromedriver.exe"; // need to find the filepath
   
   public static final String URL = "http://localhost:3000";
   public static final String TEST_USER_EMAIL = "test@csumb.edu";
   public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
   public static final int SLEEP_DURATION = 1000; // 1 second.
   
   public static final String TEST_ASSIGNMENT_NAME = "Test Assignment (Selenium)";
   public static final String TEST_ASSIGNMENT_DUEDATE = "10-31-2023";
   public static final int TEST_COURSEID = 123456;
   public static final int TEST_ASSIGNMENT_ID = 1;
   
   @Autowired
   EnrollmentRepository enrollmentRepo;
   
   @Autowired
   CourseRepository courseRepo;
   
   @Autowired
   AssignmentGradeRepository assignmentGradeRepo;
   
   @Autowired
   AssignmentRepository assignmentRepo;
   
   
   @Test
   public void testCreateAssignment() throws Exception {
      
      System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--remote-allow-origins=*");
      WebDriver driver = new ChromeDriver(options);
      
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get(URL);
      Thread.sleep(SLEEP_DURATION);
      
      try {
         // find + click on addButton + wait
         WebElement element = driver.findElement(By.id("addButton"));
         element.click();
         Thread.sleep(SLEEP_DURATION);
         
         // find aname and send TEST_ASSIGNMENT_NAME
         element = driver.findElement(By.id("aname"));
         element.sendKeys(TEST_ASSIGNMENT_NAME);
         // find duedate and send TEST_DUEDATE
         element = driver.findElement(By.id("duedate"));
         element.sendKeys(TEST_ASSIGNMENT_DUEDATE);
         // This is commented out for now because it is hard coded at the moment
         element = driver.findElement(By.id("cID"));
         element.sendKeys(Integer.toString(TEST_COURSEID));
         // find and click submit button on form
         element = driver.findElement(By.id("submit"));
         element.click();
         Thread.sleep(SLEEP_DURATION);
         
         // This will throw an error if there are multiple assignments with the same name
         Assignment a = assignmentRepo.findAssignmentByName(TEST_ASSIGNMENT_NAME);
         System.out.println("Assignment a: "+ a);
         if(a != null) {
            //assignmentRepo.deleteByName(TEST_ASSIGNMENT_NAME);
            assignmentRepo.delete(a);
         }
         
      } catch (Exception e ) {
         throw e;
      } finally {
         
         Assignment a = assignmentRepo.findAssignmentByName(TEST_ASSIGNMENT_NAME);
         System.out.println("Assignment a: "+ a);
         if(a != null) {
            //assignmentRepo.deleteByName(TEST_ASSIGNMENT_NAME);
            assignmentRepo.delete(a);
         }
         
         driver.close();
         driver.quit();
      }
   }
}
