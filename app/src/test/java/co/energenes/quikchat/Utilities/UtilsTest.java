package co.energenes.quikchat.Utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * Created by rfkamd on 7/27/2017.
 */
public class UtilsTest {

    @Test
    public void validatePhoneNumberWithDash() throws Exception {
        String str = "+966-580255946";
        boolean result = Utils.validatePhoneNumber(str);
        assertEquals(true, result);
    }




}