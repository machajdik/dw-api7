package dw;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dw.data.DataReader;
import dw.data.Transaction;

@RestController
public class PredictionController {

	@RequestMapping("/prediction")
	public ResponseEntity<Prediction> prediction(@RequestParam(value = "account", required = true) String accountId,
			@RequestParam(value = "reader", defaultValue = "DemoDataReader") String readerName, 
			@RequestParam(value = "it", defaultValue = "true") String readFromIT, HttpServletResponse  response) {

		// Access-Control-Allow-Origin: *
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		DataReader reader;
		try {
			reader = (DataReader) Class.forName("dw.data." + readerName).newInstance();
		} catch (Exception e) {
			return new ResponseEntity<Prediction>((Prediction)new Prediction().error(e.getClass().getName() + ": " + e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List<Account> accounts = reader.readData();
		for (Account a : accounts) {
			Iterator<Transaction> iter = a.getTransactions().iterator();
			while (iter.hasNext()) {
				Transaction t = iter.next();
				if (t.getAmount().signum() == 0)
					iter.remove();
			}
		}
		
		new DispoWarner().run(accounts, Boolean.valueOf(readFromIT));

		Account a = null;
		for (Account account : accounts) {
			if (account.getId().equals(accountId)) {
				a = account;
			}
		}

		if (a == null)
			return new ResponseEntity<Prediction>(HttpStatus.NOT_FOUND);

		Prediction p = new Prediction();
		LocalDate now = LocalDate.now();
		p.setBalanceNow(a.getBalance(now));
		
		// TODO set zero date
		if (a.getBalance(now).signum() <= 0) {
			p.setDateZero(java.sql.Date.valueOf(now));
		} else {
			List<LocalDate> list = new ArrayList<>();
			list.addAll(a.getFuture().keySet());
			Collections.sort(list);
			for (LocalDate date : list) {
				if (a.getFuture().get(date).signum() <= 0) {
					p.setDateZero(java.sql.Date.valueOf(date));
					break;
				}
			}
		}
			
		
		// set future and past
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, BigDecimal> future = new HashMap<>();
		for (LocalDate date : a.getFuture().keySet()) {
			future.put(df.format(java.sql.Date.valueOf(date)), a.getFuture().get(date));
		}
		p.setFuture(future);
		Map<String, BigDecimal> past = new HashMap<>();
		LocalDate date = a.getFirstTransaction().getDate();
		while (date.isBefore(now) || date.isEqual(now)) {
			past.put(df.format(java.sql.Date.valueOf(date)), a.getBalance(date));
			date = date.plusDays(1);
		}
		p.setPast(past);

		return new ResponseEntity<Prediction>(p, HttpStatus.OK);
	}
}
