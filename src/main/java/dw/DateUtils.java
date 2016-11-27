package dw;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtils {
	
	private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.getDefault()); 
	

	public static int getWeekOfYear(LocalDate date) {
		return date.get(WEEK_FIELDS.weekOfWeekBasedYear());
	}
	
	public static boolean isSameWeekAndYear(LocalDate date1, LocalDate date2) {
		return date1.getYear() == date2.getYear() && getWeekOfYear(date1) == getWeekOfYear(date2);
	}
	
}
