package dw;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

public class RecurringTransaction {

	@Getter
	private BigDecimal amount;

	@Getter
	@Setter
	private LocalDate startDate;

	@Getter
	@Setter
	private LocalDate endDate;

	@Getter
	@Setter
	private BigDecimal everyDays;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String iban;

	public RecurringTransaction(BigDecimal amount, LocalDate startDate, LocalDate endDate, BigDecimal everyDays,
			String description, String iban) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.everyDays = everyDays == null ? new BigDecimal(365 / 12) : everyDays;
		this.description = description;
		this.iban = iban;

		setAmount(amount);
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public LocalDate getLastExecutionDate(LocalDate now) {
		BigDecimal plusDays = everyDays;

		while (!startDate.plusDays(plusDays.intValue()).isAfter(now)) {
			plusDays = plusDays.add(everyDays);
		}
		plusDays = plusDays.subtract(everyDays);
		return startDate.plusDays(plusDays.intValue());
	}

}
