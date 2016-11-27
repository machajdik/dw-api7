package dw.data;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import dw.Account;

public class RandomDataGenerator implements DataReader {

	@Override
	public List<Account> readData() {
		
		if (true)
			throw new RuntimeException("no longer used");

//		System.out.println("Creating demo data... ");
//
//		RecurringTransaction gehalt = new RecurringTransaction(new BigDecimal(2000), LocalDate.of(2016, 1, 5), null,
//				null, "Gehalt", "DE11111111111111111111");
//		RecurringTransaction miete = new RecurringTransaction(new BigDecimal(-800), LocalDate.of(2016, 1, 7), null,
//				null, "Miete", "DE22222222222222222222");
//		RecurringTransaction handy = new RecurringTransaction(new BigDecimal(-30), LocalDate.of(2016, 1, 25), null,
//				null, "Handy", "DE33333333333333333333");
//		RecurringTransaction versicherung = new RecurringTransaction(new BigDecimal(-300), LocalDate.of(2016, 1, 9),
//				null, null, "Versicherung", "DE44444444444444444444");
//		RecurringTransaction leasing = new RecurringTransaction(new BigDecimal(-250), LocalDate.of(2016, 1, 15), null,
//				null, "Leasing", "DE55555555555555555555");
//		Account a = new Account("1", gehalt, miete, handy, versicherung, leasing);
//		a.setStartBalance(new BigDecimal(900));
//
//		try {
//			int year = 2016;
//
//			for (int month = 1; month < 12; month++) {
//				// wiederkehrend
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(4, 7)), "ACME Inc.", "Gehalt",
//						"DE11111111111111111111", new BigDecimal(2000), gehalt));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(6, 9)), "Vermieter Max Müller",
//						"Miete Wohnung", "DE22222222222222222222", new BigDecimal(-800), miete));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(24, 27)), "T-Mobile",
//						"Handyrechnung", "DE33333333333333333333", new BigDecimal(-25), handy));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(8, 11)), "Krankenkasse",
//						"Versicherung", "DE44444444444444444444", new BigDecimal(-300), versicherung));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(14, 17)), "Tesla",
//						"Leasingrate", "DE55555555555555555555", new BigDecimal(-250), leasing));
//
//				// laufend
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(1, 6)), "Geldautomat",
//						"Barabhebung", createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(5, 10)), "McDonalds", "Essen",
//						createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(9, 14)), "Geldautomat",
//						"Barabhebung", createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(13, 18)), "Burger King",
//						"Essen", createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(17, 22)), "Geldautomat",
//						"Barabhebung", createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(21, 26)), "KFC", "Essen",
//						createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//				add(a, new RawTransaction(LocalDate.of(year, month, RandomUtils.nextInt(25, 30)), "Geldautomat",
//						"Barabhebung", createIban(true), new BigDecimal(RandomUtils.nextDouble(50, 150) * -1), null));
//			}
//
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//		DataWriter.writePast(a, LocalDate.now());
//
//		return Collections.singletonList(a);
		return null;

	}

	private String createIban(boolean withNull) {
		if (withNull && RandomUtils.nextBoolean())
			return null;
		return "DE" + RandomStringUtils.randomNumeric(20);
	}

	private void add(Account a, Transaction t) throws IOException {
		if (t.getDate().isBefore(LocalDate.now())) {
			a.addTransaction(t);

		}
	}

}
