package dw;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

public class Prediction extends Response {
	
	@Getter
	@Setter
	private BigDecimal balanceNow;
	
	@Getter
	@Setter
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date dateZero;
	
	@Getter
	@Setter
	private Map<String, BigDecimal> future;

	@Getter
	@Setter
	private Map<String, BigDecimal> past;


}
