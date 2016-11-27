package dw.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import com.opencsv.CSVReader;

import dw.Account;


public class CSVDataReader implements DataReader {
	
	private static final String PATH = "C:\\java\\postbank\\n26.csv";
	private static final char SEPARATOR = ',';
	private static final char QUOTE = '"';
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final int START_LINE = 1;
	
	private static final int INDEX_DATE = 0;
	private static final int INDEX_NAME = 1;
	private static final int INDEX_TEXT = 4;
	private static final int INDEX_IBAN = 2;
	private static final int INDEX_AMOUNT = 6;
	
	@Override
	public List<Account> readData() {
		
		Account a = new Account("csv");
		
		try {
			CSVReader csv = new CSVReader(new FileReader(PATH), SEPARATOR, QUOTE, START_LINE);
			for (String[] f : csv.readAll()) {
				LocalDate date = LocalDate.parse(f[INDEX_DATE], DateTimeFormatter.ofPattern(DATE_FORMAT));
				Transaction t = new Transaction(RandomStringUtils.randomAlphanumeric(10), date, f[INDEX_NAME], f[INDEX_TEXT], f[INDEX_IBAN], new BigDecimal(f[INDEX_AMOUNT]));
				a.addTransaction(t);
			}
			csv.close();
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return Collections.singletonList(a);
	}
	
	
	

}
