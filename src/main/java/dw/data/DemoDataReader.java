package dw.data;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import dw.Account;

public class DemoDataReader implements DataReader {

	private static final String SOURCE = "https://raw.githubusercontent.com/a2800276/postbankdatagen/master/demo.long.json";

	@Override
	public List<Account> readData() {

		List<Account> ret = new ArrayList<>();

		Gson gson = new Gson();
		try {
			@SuppressWarnings("serial")
			Type mapType = new TypeToken<Map<String, List<Transaction>>>() {
			}.getType();
			// Map<String, List<Transaction>> map = gson.fromJson(HttpRetryUtils.getInstance().getString(SOURCE), mapType);
			Map<String, List<Transaction>> map = gson.fromJson(new FileReader("C:\\java\\postbank\\demo.long.json"), mapType);

			for (String accountId : map.keySet()) {
				System.out.println("Read Account: " + accountId);
				Account s = new Account(accountId);

				Transaction first = null;
				
				for (Transaction t : map.get(accountId)) {
					if (first == null || t.getBookingdate() < first.getBookingdate())
						first = t;
					StringBuilder text = new StringBuilder();
					for (String p : t.getPurpose()) {
						text.append(p);
					}
					s.addTransaction(t);
				}
				s.setStartBalance(first.getBalance());
				
				ret.add(s);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

}
