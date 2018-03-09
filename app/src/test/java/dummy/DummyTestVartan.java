package dummy;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Vartan Benohanian on 2018-02-12.
 */

public class DummyTestVartan {

    @Test
    public void testVartan(){

        String first = "vartan";
        String last = "beno";
        String githubID = first + last;

        assertEquals("vartanbeno", githubID);

    }

}
