package dw.utils;

public class Utils {

	public static void sleep(long millis) {
		if (millis <= 0)
			return;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
	
}