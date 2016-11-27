package dw.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dw.Account;

public class DataWriter {

	private static final String PATH = "c:\\java\\postbank\\";

	public static void writePast(Account a, LocalDate now) {

		try {
			File file = new File(PATH + "past_" + a.getId() + ".txt");
			if (file.exists())
				file.delete();
			file.createNewFile();

			FileWriter fileWriter = new FileWriter(file);

			LocalDate date = a.getFirstTransaction().getDate();
			while (date.isBefore(now) || date.isEqual(now)) {
				fileWriter.write(date + ";" + a.getBalance(date).toString().replace(".", ",") + "\n");
				date = date.plusDays(1);
			}

			fileWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static void writeFuture(Account a) {
		Map<LocalDate, BigDecimal> future = a.getFuture();
		try {
			File file = new File(PATH + "future_" + a.getId() + ".txt");
			if (file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);

			List<LocalDate> dates = new ArrayList<>(future.keySet());
			Collections.sort(dates);
			for (LocalDate date : dates) {
				fileWriter.write(date + ";" + future.get(date).toString().replace(".", ",") + "\n");
			}

			fileWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
