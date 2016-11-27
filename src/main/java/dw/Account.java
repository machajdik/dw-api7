package dw;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import dw.data.Transaction;
import it.AnalysisResponse;
import it.IntelligentTransactionsClient;
import lombok.Getter;
import lombok.Setter;

public class Account {

	@Getter
	private String id;

	@Getter
	private BigDecimal weeklySpending;

	@Getter
	@Setter
	private BigDecimal startBalance;

	@Getter
	private List<RecurringTransaction> recurringTransactions = new ArrayList<>();

	@Getter
	@Setter
	private List<Transaction> transactions = new ArrayList<>();

	@Getter
	@Setter
	private Map<LocalDate, BigDecimal> future;

	public Account(String id, RecurringTransaction... recurringTransactions) {
		this.id = id;

		this.recurringTransactions.addAll(Arrays.asList(recurringTransactions));
	}

	private boolean isRecurringTransactionDone(LocalDate now, RecurringTransaction recurringTransaction) {
		for (Transaction raw : getTransactions(now.minusDays(DispoWarner.MAX_RECURRING_WAIT), now)) {
			if (raw.getRecurringTransaction() != null && raw.getRecurringTransaction().equals(recurringTransaction))
				return true;
		}
		return false;
	}

	private List<Transaction> getTransactions(LocalDate startDate, LocalDate endDate) {
		List<Transaction> ret = new ArrayList<>();
		for (Transaction rt : getTransactions()) {
			if ((rt.getDate().isAfter(startDate) || rt.getDate().equals(startDate))
					&& (rt.getDate().isBefore(endDate) || rt.getDate().equals(endDate)))
				ret.add(rt);
		}
		return ret;
	}

	private List<Transaction> getTransactionsWeekly(int year, int weekOfYear) {
		WeekFields weekFields = WeekFields.of(Locale.getDefault());

		List<Transaction> ret = new ArrayList<>();
		for (Transaction rt : getTransactions()) {
			if (rt.getDate().get(weekFields.weekOfWeekBasedYear()) == weekOfYear && rt.getDate().getYear() == year)
				ret.add(rt);
		}
		return ret;
	}

	public List<RecurringTransaction> getOpenRecurringTransactions(LocalDate date, Set<RecurringTransaction> ignore) {
		List<RecurringTransaction> ret = new ArrayList<>();
		LocalDate minAge = date.minusDays(DispoWarner.MAX_RECURRING_WAIT);

		Set<RecurringTransaction> remaining = new HashSet<>(recurringTransactions);
		remaining.removeAll(ignore);
		for (RecurringTransaction rt : remaining) {
			LocalDate execDate = rt.getLastExecutionDate(date);
			if (!isRecurringTransactionDone(date, rt) && (execDate.isBefore(date) || execDate.isEqual(date))
					&& execDate.isAfter(minAge))
				ret.add(rt);
		}

		return ret;
	}

	/**
	 * @return amount spent (non-recurring) this week (positive amount!)
	 */
	public BigDecimal getWeeklySpent(LocalDate date) {
		BigDecimal ret = BigDecimal.ZERO;

		for (Transaction t : getTransactionsWeekly(date.getYear(), DateUtils.getWeekOfYear(date))) {
			if (t.getRecurringTransaction() == null && t.isNegative())
				ret = ret.add(t.getAmount().abs());
		}

		return ret;
	}

	public void addTransaction(Transaction rawTransaction) {
		if (rawTransaction.getAmount().signum() != 0)
			this.transactions.add(rawTransaction);
	}

	public Transaction getTransaction(String id) {
		for (Transaction t : transactions) {
			if (t.getTransactionId().equals(id))
				return t;
		}
		return null;
	}

	public void reinitRecurringTransactions(LocalDate now, boolean readFromIT) {
		recurringTransactions.clear();

		LocalDate minDate = null, maxDate = null;
		List<Transaction> rawTransactionsThatAreRecurring = new ArrayList<>();

		if (readFromIT) {
			Map<String, AnalysisResponse> map = IntelligentTransactionsClient.analyse(transactions);
			if (map != null) {
				for (String id : map.keySet()) {
					getTransaction(id).setRecurringScore(map.get(id).getRecurring_monthly());
				}
			}
		}

		Map<String, Map<LocalDate, BigDecimal>> recur = new HashMap<>();
		for (Transaction rt : transactions) {
			String key = rt.getIban() == null ? rt.getName() : rt.getIban();
			if (StringUtils.isEmpty(key))
				continue;

			recur.putIfAbsent(key, new HashMap<>());
			Map<LocalDate, BigDecimal> dates = recur.get(key);
			dates.putIfAbsent(rt.getDate(), BigDecimal.ZERO);
			dates.put(rt.getDate(), dates.get(rt.getDate()).add(rt.getAmount()));
			rawTransactionsThatAreRecurring.add(rt);

			if (minDate == null || rt.getDate().isBefore(minDate))
				minDate = rt.getDate();
			if (maxDate == null || rt.getDate().isAfter(maxDate))
				maxDate = rt.getDate();
		}

		long numDays = ChronoUnit.DAYS.between(minDate, maxDate);
		if (numDays < 61)
			return;

		for (String key : recur.keySet()) {
			Map<LocalDate, BigDecimal> dates = recur.get(key);

			// es müssen min. 2 sein, und es müssen min. 10 pro jahr sein
			if (dates.size() < 2)
				continue;

			// abstand muss mindestens 6 tage sein
			BigDecimal avgDiff = getAverageDurationDays(dates.keySet());
			if (avgDiff.compareTo(new BigDecimal(6)) < 0)
				continue;

			// wenn der abstand von der letzten ausführung zu heute zu groß
			// ist (abstand*1.2), endDate setzen
			LocalDate lastDay = getLast(dates.keySet());
			LocalDate endDate = null;
			if (lastDay.plusDays(avgDiff.multiply(new BigDecimal(1.2)).intValue()).isBefore(now))
				endDate = lastDay;

			// die zeitspanne zwischen den werten darf max. 1 jahr sein und max.
			// 1/3 des gesamtzeitraums
			if (!checkDates(dates.keySet()))
				continue;

			// jeder betrag darf nicht zu sehr von seinem vorgänger abweichen
			BigDecimal medianAmount = getMedian(dates.values());
			if (!checkAmounts(dates, medianAmount))
				continue;

			RecurringTransaction rt = new RecurringTransaction(medianAmount, minDate, endDate, avgDiff, key, key);
			recurringTransactions.add(rt);
			for (Transaction raw : rawTransactionsThatAreRecurring) {
				if (raw.getIban().equals(rt.getIban())) {
					raw.setRecurringTransaction(rt);
					if (readFromIT)
						System.out.println("Found recurring: " + raw.getTransactionId() + ", score from IT: "
								+ raw.getRecurringScore());
				}
			}
		}

		System.out.println("Found " + recurringTransactions.size() + " recurring transactions.");
	}

	private LocalDate getLast(Collection<LocalDate> dates) {
		LocalDate last = null;
		for (LocalDate date : dates) {
			if (last == null || date.isAfter(last))
				last = date;
		}
		return last;
	}

	private boolean checkAmounts(Map<LocalDate, BigDecimal> values, BigDecimal avgValue) {
		List<LocalDate> sortedDates = new ArrayList<LocalDate>(values.keySet());
		Collections.sort(sortedDates);

		List<BigDecimal> valuesSortedByDate = new ArrayList<BigDecimal>();
		for (LocalDate date : sortedDates) {
			valuesSortedByDate.add(values.get(date));
		}

		// in blöcken zu je 6 den jeweils größten ausreißer entfernen
		List<BigDecimal> valuesSortedByDateWithoutOutliers = valuesSortedByDate.size() < 6 ? valuesSortedByDate
				: unChunkWithoutOutliers(chunk(valuesSortedByDate, 6));

		// checken ob der jeweilig nächste nicht zu sehr vom vorigen abweicht
		// (ohne ausreißer)
		BigDecimal lastValue = null;
		for (BigDecimal v : valuesSortedByDateWithoutOutliers) {
			if (lastValue != null && lastValue.signum() != 0 && v.abs().subtract(lastValue.abs()).abs()
					.divide(lastValue.abs(), MathContext.DECIMAL128).doubleValue() > DispoWarner.MAX_REURRING_DEVIATION)
				return false;
			lastValue = v;
		}
		return true;
	}

	private <E> List<List<E>> chunk(List<E> list, int chunkSize) {
		if (list.size() < chunkSize)
			throw new RuntimeException("Nothing to chunk here");
		if (list.size() == chunkSize)
			return Collections.singletonList(list);

		List<List<E>> chunks = new ArrayList<>();

		List<E> chunk = new ArrayList<>();

		for (E e : list) {
			chunk.add(e);
			if (chunk.size() == chunkSize) {
				chunks.add(new ArrayList<>(chunk));
				chunk.clear();
			}
		}

		if (!chunk.isEmpty())
			chunks.get(chunks.size() - 1).addAll(chunk);

		return chunks;
	}

	private List<BigDecimal> unChunkWithoutOutliers(List<List<BigDecimal>> chunks) {
		List<BigDecimal> ret = new ArrayList<>();
		for (List<BigDecimal> chunk : chunks) {
			removeOutlier(chunk);
			ret.addAll(chunk);
		}
		return ret;
	}

	private void removeOutlier(List<BigDecimal> values) {
		BigDecimal median = getMedian(values);
		BigDecimal outlier = null;
		for (BigDecimal v : values) {
			if (outlier == null || median.subtract(v).abs().compareTo(median.subtract(outlier).abs()) > 0)
				outlier = v;
		}
		values.remove(outlier);
	}

	private BigDecimal getAverageDurationDays(Set<LocalDate> dates) {
		List<BigDecimal> durations = new ArrayList<>();
		List<LocalDate> dateList = new ArrayList<>(dates);
		Collections.sort(dateList);
		LocalDate lastDate = null;
		for (LocalDate date : dateList) {
			if (lastDate != null) {
				durations.add(new BigDecimal(ChronoUnit.DAYS.between(lastDate, date)));
			}
			lastDate = date;
		}
		return getAverage(durations);
	}

	private BigDecimal getAverage(Collection<BigDecimal> values) {
		return sumAll(values).divide(new BigDecimal(values.size()), MathContext.DECIMAL128);
	}

	private BigDecimal getMedian(Collection<BigDecimal> values) {
		List<BigDecimal> valueList = new ArrayList<>(values);
		Collections.sort(valueList);
		return valueList.get(values.size() / 2);
	}

	private boolean checkDates(Set<LocalDate> dates) {
		List<LocalDate> dateList = new ArrayList<>(dates);
		Collections.sort(dateList);
		LocalDate lastDate = null;
		for (LocalDate date : dateList) {
			if (lastDate != null && ChronoUnit.DAYS.between(date, lastDate) > 366)
				return false;
		}
		return true;
	}

	public void reinitWeeklySpending() {

		LocalDate now = LocalDate.now();
		Map<String, BigDecimal> spentPerWeek = new HashMap<>();
		for (Transaction rt : transactions) {
			if (rt.getRecurringTransaction() == null && !DateUtils.isSameWeekAndYear(now, rt.getDate())) {
				String key = rt.getDate().getYear() + "-" + DateUtils.getWeekOfYear(rt.getDate());
				spentPerWeek.put(key, spentPerWeek.getOrDefault(key, BigDecimal.ZERO).add(rt.getAmount()));
			}
		}

		this.weeklySpending = sumAll(spentPerWeek.values())
				.divide(new BigDecimal(spentPerWeek.size()), MathContext.DECIMAL128).abs()
				.setScale(2, BigDecimal.ROUND_HALF_UP);

		System.out.println("Weekly spending: " + this.weeklySpending);
	}

	private BigDecimal sumAll(Collection<BigDecimal> values) {
		BigDecimal ret = BigDecimal.ZERO;
		for (BigDecimal v : values) {
			ret = ret.add(v);
		}
		return ret;
	}

	public BigDecimal getBalance(LocalDate date) {
		BigDecimal balance = startBalance == null ? BigDecimal.ZERO : startBalance;
		for (Transaction t : getTransactions()) {
			if (t.getDate().isBefore(date) || t.getDate().equals(date))
				balance = balance.add(t.getAmount());
		}
		return balance;
	}

	public RecurringTransaction getRecurringTransaction(String iban) {
		for (RecurringTransaction t : getRecurringTransactions()) {
			if (iban.equals(t.getIban()))
				return t;
		}
		return null;
	}

	public Transaction getFirstTransaction() {
		Transaction first = null;
		for (Transaction t : getTransactions()) {
			if (first == null || t.getDate().isBefore(first.getDate()))
				first = t;
		}
		return first;
	}

	public Transaction getLastTransaction() {
		Transaction last = null;
		for (Transaction t : getTransactions()) {
			if (last == null || t.getDate().isAfter(last.getDate()))
				last = t;
		}
		return last;
	}

}
