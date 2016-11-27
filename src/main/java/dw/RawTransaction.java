package dw;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Deprecated
public class RawTransaction {

	@Getter
	@Setter
	private LocalDate date;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String text;
	@Getter
	@Setter
	private String iban;
	
	@Getter
	private BigDecimal amount;
	
	@Getter
	@Setter
	private RecurringTransaction recurringTransaction;
	
	


	

}
