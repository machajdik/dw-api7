package dw.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import dw.RecurringTransaction;
import lombok.Getter;
import lombok.Setter;

public class Transaction {

	@Setter
	@Getter
	private String transactionId;
	@Getter
	private BigDecimal amount;
	@Getter
	private BigDecimal balance;
	@Setter
	@Getter
	private List<String> purpose;
	@Setter
	@Getter
	private long bookingdate;
	@Setter
	@Getter
	private Reference reference = new Reference();
	@Setter
	@Getter
	private Classification __classification = new Classification();
	@Getter
	@Setter
	private RecurringTransaction recurringTransaction;
	
	@Getter
	@Setter
	private double recurringScore;


	public void setAmount(BigDecimal amount) {
		this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public Transaction() {
		
	}
	
	public Transaction(String id, LocalDate date, String name, String text, String iban, BigDecimal amount) {
		this(id,date, name, text, iban, amount, null);
	}
		
	public Transaction(String id, LocalDate date, String name, String text, String iban, BigDecimal amount, RecurringTransaction recurringTransaction) {
		transactionId = id;
		ZoneId zoneId = ZoneId.systemDefault(); 
		this.bookingdate = date.atStartOfDay(zoneId).toEpochSecond();
		this.reference.setName(name);
		this.purpose = Collections.singletonList(text);
		this.reference.setIban(iban);
		this.__classification.setRecurring_monthly(recurringTransaction != null);
		this.recurringTransaction = recurringTransaction;
		
		setAmount(amount);
	}
	
	public boolean isNegative() {
		return amount.signum() < 0;
	}
	
	public LocalDate getDate() {
		return Instant.ofEpochSecond(bookingdate).atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public String getIban() {
		return reference.getIban();
	}
	
	public String getName() {
		return reference.getName();
	}
	

}
