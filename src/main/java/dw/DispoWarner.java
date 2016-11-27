package dw;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dw.file.DataWriter;

public class DispoWarner {

	// how long do we wait for an open recurring transaction before discarding
	// it?
	public static final int MAX_RECURRING_WAIT = 5;
	
	// maximal erlaubter unterschied bei recurring transactions (0.1=10%)
	public static final double MAX_REURRING_DEVIATION = 0.2;
	
	private static final int FUTURE_DAYS = 365;

	public void run(List<Account> accounts, boolean readFromIT) {

		LocalDate now = LocalDate.now();
		
		for (Account a : accounts) {
			System.out.println("Processing Account: " + a.getId());
			a.reinitRecurringTransactions(now, readFromIT);
			a.reinitWeeklySpending();
			
			DataWriter.writePast(a, now);
			a.setFuture(calculateFuture(a, now, FUTURE_DAYS));
			DataWriter.writeFuture(a);
			
		}
		
		System.out.println("All done");
		
		
//		Account demo = new DemoData().createDemoAccount();
//		demo.reinitRecurringTransactions();
//		demo.reinitWeeklySpending();
//		
//		LocalDate now = LocalDate.now();
//		System.out.println("Balance as of " + now + ": " + demo.getBalance(now));
//
//		Map<LocalDate, BigDecimal> future = calculateFuture(demo, now, 120);
//		List<LocalDate> dates = new ArrayList<LocalDate>(future.keySet());
//		Collections.sort(dates);
//		for (LocalDate date : dates) {
//			BigDecimal balance = future.get(date);
//			if (balance.signum() <= 0) {
//				System.out.println("You are broke on " + date + ", balance will be " + balance);
//				break;
//			}
//
//		}
		

	}

	/**
	 * @return future balance for given number of days
	 */
	protected Map<LocalDate, BigDecimal> calculateFuture(Account account, LocalDate now, int numberOfDays) {

		System.out.println("Calculating future... ");

		Map<LocalDate, BigDecimal> future = new HashMap<LocalDate, BigDecimal>();
		Map<RecurringTransaction, LocalDate> alreadyConsidered = new HashMap<>();
		
		BigDecimal currentBalance = account.getBalance(now);
		for (int i = 0; i <= numberOfDays; i++) {

			LocalDate currentDate = now.plusDays(i);

			// alreadyConsidered: die die älter als der max abstand sind, rauswerfen
			Set<RecurringTransaction> toDelete = new HashSet<>();
			for (RecurringTransaction oldRec : alreadyConsidered.keySet()) {
				if (alreadyConsidered.get(oldRec).isBefore(currentDate.minusDays(MAX_RECURRING_WAIT + 1)))
					toDelete.add(oldRec);
			}
			alreadyConsidered.keySet().removeAll(toDelete);

			// subtract recurring transactions that where not considered yet
			List<RecurringTransaction> openTransactions = account.getOpenRecurringTransactions(currentDate, alreadyConsidered.keySet());
			for (RecurringTransaction rt : openTransactions) {
				currentBalance = currentBalance.add(rt.getAmount());
				alreadyConsidered.put(rt, currentDate);
			}

			// subtract weekly spendings if not already spent
			if (isSameWeek(now, currentDate)) {
				// what did we already spend?
				BigDecimal spent = account.getWeeklySpent(currentDate);
				BigDecimal avgSpend = account.getWeeklySpending();
				if (spent.compareTo(avgSpend) < 0) {
					// only if we didn't spend everything already
					int numOfDaysLeft = DayOfWeek.SUNDAY.getValue() + 1 - now.getDayOfWeek().getValue();
					if (numOfDaysLeft > 0) {
						// only if there are still days left
						currentBalance = currentBalance
								.subtract(avgSpend.subtract(spent).divide(new BigDecimal(numOfDaysLeft)));
					}
				}
			} else {
				// no current spending
				BigDecimal avgSpend = account.getWeeklySpending();
				currentBalance = currentBalance.subtract(avgSpend.divide(new BigDecimal(7), MathContext.DECIMAL128));
			}

			future.put(currentDate, currentBalance.setScale(2, BigDecimal.ROUND_HALF_UP));
		}

		System.out.println("finished");
		return future;

	}

	private boolean isSameWeek(LocalDate date1, LocalDate date2) {
		return date1.getYear() == date2.getYear() && DateUtils.getWeekOfYear(date1) == DateUtils.getWeekOfYear(date2);
	}

}
